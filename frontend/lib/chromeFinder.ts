import { execSync } from "child_process";
import { existsSync } from "fs";

/**
 * Utility để tìm đường dẫn Chrome trên hệ thống
 * Hỗ trợ Windows, macOS, và Linux
 */
export function getChromePath(): string {
    const platform = process.platform;

    try {
        if (platform === "win32") {
            return findChromeOnWindows();
        } else if (platform === "darwin") {
            return findChromeOnMac();
        } else {
            return findChromeOnLinux();
        }
    } catch (error) {
        console.error("Error finding Chrome:", error);
        throw new Error(
            "Chrome not found. Please install Google Chrome on your system.\n" +
            "Download: https://www.google.com/chrome/\n" +
            "Windows: https://www.google.com/chrome/\n" +
            "macOS: https://www.google.com/chrome/\n" +
            "Linux: sudo apt install google-chrome-stable"
        );
    }
}

/**
 * Tìm Chrome trên Windows
 */
function findChromeOnWindows(): string {
    const possiblePaths = [
        "C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe",
        "C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe",
        process.env.LOCALAPPDATA + "\\Google\\Chrome\\Application\\chrome.exe",
        process.env.PROGRAMFILES + "\\Google\\Chrome\\Application\\chrome.exe",
        process.env["PROGRAMFILES(X86)"] + "\\Google\\Chrome\\Application\\chrome.exe",
    ];

    // Kiểm tra các đường dẫn phổ biến
    for (const path of possiblePaths) {
        if (path && existsSync(path)) {
            return path;
        }
    }

    // Thử tìm bằng where command
    try {
        const result = execSync("where chrome", { encoding: "utf-8" }).trim();
        if (result) {
            const firstPath = result.split("\n")[0].trim();
            if (existsSync(firstPath)) {
                return firstPath;
            }
        }
    } catch {
        // Ignore error
    }

    throw new Error("Chrome not found on Windows");
}

/**
 * Tìm Chrome trên macOS
 */
function findChromeOnMac(): string {
    const possiblePaths = [
        "/Applications/Google Chrome.app/Contents/MacOS/Google Chrome",
        process.env.HOME + "/Applications/Google Chrome.app/Contents/MacOS/Google Chrome",
    ];

    for (const path of possiblePaths) {
        if (path && existsSync(path)) {
            return path;
        }
    }

    throw new Error("Chrome not found on macOS");
}

/**
 * Tìm Chrome trên Linux
 */
function findChromeOnLinux(): string {
    // Kiểm tra biến môi trường PUPPETEER_EXECUTABLE_PATH trước (cho Docker/Alpine)
    const envPath = process.env.PUPPETEER_EXECUTABLE_PATH;
    if (envPath && existsSync(envPath)) {
        return envPath;
    }

    const possiblePaths = [
        "/usr/bin/chromium-browser", // Alpine Linux
        "/usr/bin/chromium",         // Alpine Linux alternative
        "/usr/bin/google-chrome",
        "/usr/bin/google-chrome-stable",
        "/snap/bin/chromium",
    ];

    for (const path of possiblePaths) {
        if (existsSync(path)) {
            return path;
        }
    }

    // Thử tìm bằng which command
    for (const binary of ["chromium-browser", "chromium", "google-chrome", "google-chrome-stable"]) {
        try {
            const result = execSync(`which ${binary}`, { encoding: "utf-8" }).trim();
            if (result && existsSync(result)) {
                return result;
            }
        } catch {
            // Ignore error
        }
    }

    throw new Error("Chrome not found on Linux");
}

/**
 * Validate Chrome path
 */
export function validateChromePath(path: string): boolean {
    return existsSync(path);
}
