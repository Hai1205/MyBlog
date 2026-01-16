"use client";

import { useEffect } from "react";

export function CookieMonitor() {
  useEffect(() => {
    if (process.env.NODE_ENV !== "development") {
      return;
    }

    // Monitor cookie changes
    let lastCookies = document.cookie;
    const interval = setInterval(() => {
      const currentCookies = document.cookie;
      if (currentCookies !== lastCookies) {
        
        // Parse and show difference
        parseCookies(lastCookies);
        parseCookies(currentCookies);
        
        lastCookies = currentCookies;
      }
    }, 1000);

    return () => clearInterval(interval);
  }, []);

  return null;
}

function parseCookies(cookieString: string): Record<string, string> {
  const cookies: Record<string, string> = {};
  cookieString.split(';').forEach(cookie => {
    const [name, value] = cookie.trim().split('=');
    if (name && value) {
      cookies[name] = value;
    }
  });
  
  return cookies;
}
