PS C:\Users\ASUS\OneDrive\Desktop\Learn\MERN\Javascript\ReactJS\NextJS\projects\MyBlog\frontend>

 npm run dev

> frontend@0.1.0 dev
> next dev --turbopack -p 3000

▲ Next.js 16.1.1 (Turbopack)
- Local:         http://localhost:3000
- Network:       http://192.168.221.1:3000
- Environments: .env

✓ Starting...
⚠ The "middleware" file convention is deprecated. Please use "proxy" instead. Learn more: https://nextjs.org/docs/messages/middleware-to-proxy
✓ Ready in 1406ms
Middleware Debug: {
  pathname: '/',
  isAuthenticated: false,
  isAdmin: false,
  userRole: null,
  hasToken: false,
  hasUserAuth: false,
  tokenPreview: 'NO TOKEN',
  allCookies: [ 'ajs_anonymous_id' ],
  userInfo: null
}
○ Compiling / ...
Error: Can't resolve 'tailwindcss' in 'C:\Users\ASUS\OneDrive\Desktop\Learn\MERN\Javascript\ReactJS\NextJS\projects\MyBlog'
    [at finishWithoutResolve (C:\Users\ASUS\OneDrive\Desktop\Learn\MERN\Javascript\ReactJS\NextJS\projects\MyBlog\frontend\node_modules\enhanced-resolve\lib\Resolver.js:565:18)]
    [at C:\Users\ASUS\OneDrive\Desktop\Learn\MERN\Javascript\ReactJS\NextJS\projects\MyBlog\frontend\node_modules\enhanced-resolve\lib\Resolver.js:657:14]
    [at C:\Users\ASUS\OneDrive\Desktop\Learn\MERN\Javascript\ReactJS\NextJS\projects\MyBlog\frontend\node_modules\enhanced-resolve\lib\Resolver.js:718:5]
    [at eval (eval at create (C:\Users\ASUS\OneDrive\Desktop\Learn\MERN\Javascript\ReactJS\NextJS\projects\MyBlog\frontend\node_modules\tapable\lib\HookCodeFactory.js:31:10), <anonymous>:15:1)]
    [at C:\Users\ASUS\OneDrive\Desktop\Learn\MERN\Javascript\ReactJS\NextJS\projects\MyBlog\frontend\node_modules\enhanced-resolve\lib\Resolver.js:718:5]
    [at eval (eval at create (C:\Users\ASUS\OneDrive\Desktop\Learn\MERN\Javascript\ReactJS\NextJS\projects\MyBlog\frontend\node_modules\tapable\lib\HookCodeFactory.js:31:10), <anonymous>:16:1)]
    [at C:\Users\ASUS\OneDrive\Desktop\Learn\MERN\Javascript\ReactJS\NextJS\projects\MyBlog\frontend\node_modules\enhanced-resolve\lib\Resolver.js:718:5]
    [at eval (eval at create (C:\Users\ASUS\OneDrive\Desktop\Learn\MERN\Javascript\ReactJS\NextJS\projects\MyBlog\frontend\node_modules\tapable\lib\HookCodeFactory.js:31:10), <anonymous>:15:1)]
    [at C:\Users\ASUS\OneDrive\Desktop\Learn\MERN\Javascript\ReactJS\NextJS\projects\MyBlog\frontend\node_modules\enhanced-resolve\lib\Resolver.js:718:5]
    [at eval (eval at create (C:\Users\ASUS\OneDrive\Desktop\Learn\MERN\Javascript\ReactJS\NextJS\projects\MyBlog\frontend\node_modules\tapable\lib\HookCodeFactory.js:31:10), <anonymous>:15:1)] {
  details: "resolve 'tailwindcss' in 'C:\\Users\\ASUS\\OneDrive\\Desktop\\Learn\\MERN\\Javascript\\ReactJS\\NextJS\\projects\\MyBlog'\n" +
    '  Parsed request is a module\n' +
    '  No description file found in C:\\Users\\ASUS\\OneDrive\\Desktop\\Learn\\MERN\\Javascript\\ReactJS\\NextJS\\projects\\MyBlog or above\n' +
    '  resolve as module\n' +
    "    C:\\Users\\ASUS\\OneDrive\\Desktop\\Learn\\MERN\\Javascript\\ReactJS\\NextJS\\projects\\MyBlog\\node_modules doesn't exist or is not a directory\n" +
    "    C:\\Users\\ASUS\\OneDrive\\Desktop\\Learn\\MERN\\Javascript\\ReactJS\\NextJS\\projects\\node_modules doesn't exist or is not a directory\n" +
    "    C:\\Users\\ASUS\\OneDrive\\Desktop\\Learn\\MERN\\Javascript\\ReactJS\\NextJS\\node_modules doesn't exist or is not a directory\n" +
    "    C:\\Users\\ASUS\\OneDrive\\Desktop\\Learn\\MERN\\Javascript\\ReactJS\\node_modules doesn't exist or is not a directory\n" +
    "    C:\\Users\\ASUS\\OneDrive\\Desktop\\Learn\\MERN\\Javascript\\node_modules doesn't exist or is not a directory\n" +
    "    C:\\Users\\ASUS\\OneDrive\\Desktop\\Learn\\MERN\\node_modules doesn't exist or is not a directory\n" +
    "    C:\\Users\\ASUS\\OneDrive\\Desktop\\Learn\\node_modules doesn't exist or is not a directory\n" +
    "    C:\\Users\\ASUS\\OneDrive\\Desktop\\node_modules doesn't exist or is not a directory\n" +
    "    C:\\Users\\ASUS\\OneDrive\\node_modules doesn't exist or is not a directory\n" +       
    '    looking for modules in C:\\Users\\ASUS\\node_modules\n' +
    '      single file module\n' +
    '        No description file found in C:\\Users\\ASUS\\node_modules or above\n' +
    '        no extension\n' +
    "          C:\\Users\\ASUS\\node_modules\\tailwindcss doesn't exist\n" +
    '        .css\n' +
    "          C:\\Users\\ASUS\\node_modules\\tailwindcss.css doesn't exist\n" +
    "      C:\\Users\\ASUS\\node_modules\\tailwindcss doesn't exist\n" +
    "    C:\\Users\\node_modules doesn't exist or is not a directory\n" +
    "    C:\\node_modules doesn't exist or is not a directory"
}
Error: Can't resolve 'tailwindcss' in 'C:\Users\ASUS\OneDrive\Desktop\Learn\MERN\Javascript\ReactJS\NextJS\projects\MyBlog'
    [at finishWithoutResolve (C:\Users\ASUS\OneDrive\Desktop\Learn\MERN\Javascript\ReactJS\NextJS\projects\MyBlog\frontend\node_modules\enhanced-resolve\lib\Resolver.js:565:18)]
    [at C:\Users\ASUS\OneDrive\Desktop\Learn\MERN\Javascript\ReactJS\NextJS\projects\MyBlog\frontend\node_modules\enhanced-resolve\lib\Resolver.js:657:14]
    [at C:\Users\ASUS\OneDrive\Desktop\Learn\MERN\Javascript\ReactJS\NextJS\projects\MyBlog\frontend\node_modules\enhanced-resolve\lib\Resolver.js:718:5]
    [at eval (eval at create (C:\Users\ASUS\OneDrive\Desktop\Learn\MERN\Javascript\ReactJS\NextJS\projects\MyBlog\frontend\node_modules\tapable\lib\HookCodeFactory.js:31:10), <anonymous>:15:1)]
    [at C:\Users\ASUS\OneDrive\Desktop\Learn\MERN\Javascript\ReactJS\NextJS\projects\MyBlog\frontend\node_modules\enhanced-resolve\lib\Resolver.js:718:5]
    [at eval (eval at create (C:\Users\ASUS\OneDrive\Desktop\Learn\MERN\Javascript\ReactJS\NextJS\projects\MyBlog\frontend\node_modules\tapable\lib\HookCodeFactory.js:31:10), <anonymous>:16:1)]
    [at C:\Users\ASUS\OneDrive\Desktop\Learn\MERN\Javascript\ReactJS\NextJS\projects\MyBlog\frontend\node_modules\enhanced-resolve\lib\Resolver.js:718:5]
    [at eval (eval at create (C:\Users\ASUS\OneDrive\Desktop\Learn\MERN\Javascript\ReactJS\NextJS\projects\MyBlog\frontend\node_modules\tapable\lib\HookCodeFactory.js:31:10), <anonymous>:15:1)]
    [at C:\Users\ASUS\OneDrive\Desktop\Learn\MERN\Javascript\ReactJS\NextJS\projects\MyBlog\frontend\node_modules\enhanced-resolve\lib\Resolver.js:718:5]
    [at eval (eval at create (C:\Users\ASUS\OneDrive\Desktop\Learn\MERN\Javascript\ReactJS\NextJS\projects\MyBlog\frontend\node_modules\tapable\lib\HookCodeFactory.js:31:10), <anonymous>:15:1)] {
  details: "resolve 'tailwindcss' in 'C:\\Users\\ASUS\\OneDrive\\Desktop\\Learn\\MERN\\Javascript\\ReactJS\\NextJS\\projects\\MyBlog'\n" +
    '  Parsed request is a module\n' +
    '  No description file found in C:\\Users\\ASUS\\OneDrive\\Desktop\\Learn\\MERN\\Javascript\\ReactJS\\NextJS\\projects\\MyBlog or above\n' +
    '  resolve as module\n' +
    "    C:\\Users\\ASUS\\OneDrive\\Desktop\\Learn\\MERN\\Javascript\\ReactJS\\NextJS\\projects\\MyBlog\\node_modules doesn't exist or is not a directory\n" +
    "    C:\\Users\\ASUS\\OneDrive\\Desktop\\Learn\\MERN\\Javascript\\ReactJS\\NextJS\\projects\\node_modules doesn't exist or is not a directory\n" +
    "    C:\\Users\\ASUS\\OneDrive\\Desktop\\Learn\\MERN\\Javascript\\ReactJS\\NextJS\\node_modules doesn't exist or is not a directory\n" +
    "    C:\\Users\\ASUS\\OneDrive\\Desktop\\Learn\\MERN\\Javascript\\ReactJS\\node_modules doesn't exist or is not a directory\n" +
    "    C:\\Users\\ASUS\\OneDrive\\Desktop\\Learn\\MERN\\Javascript\\node_modules doesn't exist or is not a directory\n" +
    "    C:\\Users\\ASUS\\OneDrive\\Desktop\\Learn\\MERN\\node_modules doesn't exist or is not a directory\n" +
    "    C:\\Users\\ASUS\\OneDrive\\Desktop\\Learn\\node_modules doesn't exist or is not a directory\n" +
    "    C:\\Users\\ASUS\\OneDrive\\Desktop\\node_modules doesn't exist or is not a directory\n" +
    "    C:\\Users\\ASUS\\OneDrive\\node_modules doesn't exist or is not a directory\n" +       
    '    looking for modules in C:\\Users\\ASUS\\node_modules\n' +
    '      single file module\n' +
    '        No description file found in C:\\Users\\ASUS\\node_modules or above\n' +
    '        no extension\n' +
    "          C:\\Users\\ASUS\\node_modules\\tailwindcss doesn't exist\n" +
    '        .css\n' +
    "          C:\\Users\\ASUS\\node_modules\\tailwindcss.css doesn't exist\n" +
    "      C:\\Users\\ASUS\\node_modules\\tailwindcss doesn't exist\n" +
    "    C:\\Users\\node_modules doesn't exist or is not a directory\n" +
    "    C:\\node_modules doesn't exist or is not a directory"
}
Error: Can't resolve 'tailwindcss' in 'C:\Users\ASUS\OneDrive\Desktop\Learn\MERN\Javascript\ReactJS\NextJS\projects\MyBlog'
    [at finishWithoutResolve (C:\Users\ASUS\OneDrive\Desktop\Learn\MERN\Javascript\ReactJS\NextJS\projects\MyBlog\frontend\node_modules\enhanced-resolve\lib\Resolver.js:565:18)]
    [at C:\Users\ASUS\OneDrive\Desktop\Learn\MERN\Javascript\ReactJS\NextJS\projects\MyBlog\frontend\node_modules\enhanced-resolve\lib\Resolver.js:657:14]
    [at C:\Users\ASUS\OneDrive\Desktop\Learn\MERN\Javascript\ReactJS\NextJS\projects\MyBlog\frontend\node_modules\enhanced-resolve\lib\Resolver.js:718:5]
    [at eval (eval at create (C:\Users\ASUS\OneDrive\Desktop\Learn\MERN\Javascript\ReactJS\NextJS\projects\MyBlog\frontend\node_modules\tapable\lib\HookCodeFactory.js:31:10), <anonymous>:15:1)]
    [at C:\Users\ASUS\OneDrive\Desktop\Learn\MERN\Javascript\ReactJS\NextJS\projects\MyBlog\frontend\node_modules\enhanced-resolve\lib\Resolver.js:718:5]
    [at eval (eval at create (C:\Users\ASUS\OneDrive\Desktop\Learn\MERN\Javascript\ReactJS\NextJS\projects\MyBlog\frontend\node_modules\tapable\lib\HookCodeFactory.js:31:10), <anonymous>:16:1)]
    [at C:\Users\ASUS\OneDrive\Desktop\Learn\MERN\Javascript\ReactJS\NextJS\projects\MyBlog\frontend\node_modules\enhanced-resolve\lib\Resolver.js:718:5]
    [at eval (eval at create (C:\Users\ASUS\OneDrive\Desktop\Learn\MERN\Javascript\ReactJS\NextJS\projects\MyBlog\frontend\node_modules\tapable\lib\HookCodeFactory.js:31:10), <anonymous>:15:1)]
    [at C:\Users\ASUS\OneDrive\Desktop\Learn\MERN\Javascript\ReactJS\NextJS\projects\MyBlog\frontend\node_modules\enhanced-resolve\lib\Resolver.js:718:5]
    [at eval (eval at create (C:\Users\ASUS\OneDrive\Desktop\Learn\MERN\Javascript\ReactJS\NextJS\projects\MyBlog\frontend\node_modules\tapable\lib\HookCodeFactory.js:31:10), <anonymous>:15:1)] {
  details: "resolve 'tailwindcss' in 'C:\\Users\\ASUS\\OneDrive\\Desktop\\Learn\\MERN\\Javascript\\ReactJS\\NextJS\\projects\\MyBlog'\n" +
    '  Parsed request is a module\n' +
    '  No description file found in C:\\Users\\ASUS\\OneDrive\\Desktop\\Learn\\MERN\\Javascript\\ReactJS\\NextJS\\projects\\MyBlog or above\n' +
    '  resolve as module\n' +
    "    C:\\Users\\ASUS\\OneDrive\\Desktop\\Learn\\MERN\\Javascript\\ReactJS\\NextJS\\projects\\MyBlog\\node_modules doesn't exist or is not a directory\n" +
    "    C:\\Users\\ASUS\\OneDrive\\Desktop\\Learn\\MERN\\Javascript\\ReactJS\\NextJS\\projects\\node_modules doesn't exist or is not a directory\n" +
    "    C:\\Users\\ASUS\\OneDrive\\Desktop\\Learn\\MERN\\Javascript\\ReactJS\\NextJS\\node_modules doesn't exist or is not a directory\n" +
    "    C:\\Users\\ASUS\\OneDrive\\Desktop\\Learn\\MERN\\Javascript\\ReactJS\\node_modules doesn't exist or is not a directory\n" +
    "    C:\\Users\\ASUS\\OneDrive\\Desktop\\Learn\\MERN\\Javascript\\node_modules doesn't exist or is not a directory\n" +
    "    C:\\Users\\ASUS\\OneDrive\\Desktop\\Learn\\MERN\\node_modules doesn't exist or is not a directory\n" +
    "    C:\\Users\\ASUS\\OneDrive\\Desktop\\Learn\\node_modules doesn't exist or is not a directory\n" +
    "    C:\\Users\\ASUS\\OneDrive\\Desktop\\node_modules doesn't exist or is not a directory\n" +
    "    C:\\Users\\ASUS\\OneDrive\\node_modules doesn't exist or is not a directory\n" +       
    '    looking for modules in C:\\Users\\ASUS\\node_modules\n' +
    '      single file module\n' +
    '        No description file found in C:\\Users\\ASUS\\node_modules or above\n' +
    '        no extension\n' +
    "          C:\\Users\\ASUS\\node_modules\\tailwindcss doesn't exist\n" +
    '        .css\n' +
    "          C:\\Users\\ASUS\\node_modules\\tailwindcss.css doesn't exist\n" +
    "      C:\\Users\\ASUS\\node_modules\\tailwindcss doesn't exist\n" +
    "    C:\\Users\\node_modules doesn't exist or is not a directory\n" +
    "    C:\\node_modules doesn't exist or is not a directory"
}
PS C:\Users\ASUS\OneDrive\Desktop\Learn\MERN\Javascript\ReactJS\NextJS\projects\MyBlog\frontend>

