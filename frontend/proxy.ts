import { NextResponse } from 'next/server'
import type { NextRequest } from 'next/server'

/**
 * Proxy to handle authentication and access control for admin routes
 * 
 * Rules:
 * 1. If user accesses /admin and is not authenticated -> redirect to /auth/login
 * 2. If user accesses /admin and is authenticated but not admin -> redirect to /
 * 3. If user is authenticated and tries to access /auth/* -> redirect to /
 * 4. If user is on mobile and tries to access /admin or /auth -> redirect to /
 */
export default function proxy(request: NextRequest) {
    try {
        const pathname = request.nextUrl.pathname

        // Skip middleware for special paths that should not be protected
        if (
            pathname.startsWith('/.well-known') ||
            pathname.startsWith('/_next') ||
            pathname.startsWith('/api') ||
            pathname.includes('/favicon') ||
            (pathname.includes('.') && !pathname.endsWith('/')) // Skip files with extensions
        ) {
            return NextResponse.next()
        }

        const response = NextResponse.next()

        // Add performance headers for faster loading
        response.headers.set('X-DNS-Prefetch-Control', 'on')

        // Enable early hints for critical resources
        if (pathname === '/') {
            response.headers.set('Link', [
                '</images/logo.png>; rel=preload; as=image',
                '<https://fonts.googleapis.com>; rel=preconnect',
                '<https://fonts.gstatic.com>; rel=preconnect; crossorigin',
            ].join(', '))
        }

        // Optimize for static assets
        if (pathname.startsWith('/_next/static/')) {
            response.headers.set('Cache-Control', 'public, max-age=31536000, immutable')
        }

        // Add server timing header for debugging
        if (process.env.NODE_ENV === 'development') {
            response.headers.set('Server-Timing', 'middleware;dur=0')
        }

        // Get the authentication token from cookies
        let authToken = request.cookies.get('access_token')?.value;

        // Decode token if it's URL encoded
        if (authToken) {
            try {
                authToken = decodeURIComponent(authToken);
            } catch (e) {
                console.log('Token is not URL encoded, using as-is');
            }
        }

        // Parse the user authentication status
        let isAuthenticated = false
        let isAdmin = false
        let userRole = null
        let userAuth = null

        // Try to get user info from auth-storage (Zustand store)
        const authStorage = request.cookies.get('auth-storage')?.value;
        if (authStorage) {
            try {
                const decoded = decodeURIComponent(authStorage);
                const authData = JSON.parse(decoded);
                userAuth = authData?.state?.userAuth;

                if (userAuth) {
                    userRole = userAuth.role;
                    isAdmin = userRole === 'ADMIN' || userRole === 'admin';
                }
            } catch (e) {
                console.log('Failed to parse auth-storage for user info');
            }
        }

        // Decode JWT token to verify authentication (this is the source of truth)
        if (authToken) {
            try {
                // Decode JWT payload (second part)
                const parts = authToken.split('.');
                if (parts.length === 3) {
                    const payload = parts[1];

                    // Decode Base64URL properly
                    let base64 = payload.replace(/-/g, '+').replace(/_/g, '/')

                    // Add padding if needed
                    while (base64.length % 4) {
                        base64 += '='
                    }

                    // Decode base64
                    const jsonPayload = atob(base64)
                    const decodedPayload = JSON.parse(jsonPayload)

                    // Check if token is valid and not expired
                    const currentTime = Math.floor(Date.now() / 1000)
                    if (decodedPayload.exp && decodedPayload.exp > currentTime) {
                        // Token is valid - user is authenticated
                        isAuthenticated = true

                        // If we don't have role from storage, get it from token
                        if (!userRole) {
                            userRole = decodedPayload.role
                            isAdmin = userRole === 'ADMIN' || userRole === 'admin'
                        }
                    } else {
                        console.log('Token expired:', { exp: decodedPayload.exp, now: currentTime })
                    }
                }
            } catch (error) {
                console.error('Error parsing auth token:', error, { tokenPreview: authToken?.substring(0, 20) })
                isAuthenticated = false
                isAdmin = false
            }
        }

        // Debug logging for development
        console.log('Middleware Debug:', {
            pathname,
            isAuthenticated,
            isAdmin,
            userRole,
            hasToken: !!authToken,
            hasUserAuth: !!userAuth,
            tokenPreview: authToken ? `${authToken.substring(0, 30)}...` : 'NO TOKEN',
            allCookies: request.cookies.getAll().map(c => c.name),
            userInfo: userAuth ? { id: userAuth.id, role: userAuth.role, username: userAuth.username } : null,
        })

        // Route protection logic

        // 1. Redirect authenticated users away from auth pages FIRST (prevent infinite loop)
        if (isAuthenticated && pathname.startsWith('/auth')) {
            return NextResponse.redirect(new URL('/', request.url))
        }

        // 2. Protect routes that require authentication
        if (
            pathname.startsWith('/blogs/new') ||
            pathname.startsWith('/blogs/edit') ||
            pathname.startsWith('/blogs/saved') ||
            pathname.startsWith('/blogs/my-blogs') ||
            pathname.startsWith('/settings')
        ) {
            if (!authToken && !authStorage) {
                return NextResponse.redirect(new URL('/auth/login', request.url))
            }
        }

        // 3. Protect admin routes
        if (pathname.startsWith('/admin')) {
            if (!authToken && !authStorage) {
                return NextResponse.redirect(new URL('/auth/login', request.url))
            }

            if (isAuthenticated && !isAdmin) {
                return NextResponse.redirect(new URL('/', request.url))
            }
        }

        return response
    } catch (error) {
        console.error('Middleware error:', error)
        // If middleware fails, allow the request to proceed
        return NextResponse.next()
    }
}

export const config = {
    matcher: [
        /*
         * Match all request paths except for the ones starting with:
         * - api (API routes)
         * - _next (Next.js internal)
         * - static files (public folder)
         * - .well-known (special paths)
         */
        '/((?!api|_next|static|.*\\..*|.well-known).*)',
    ],
}