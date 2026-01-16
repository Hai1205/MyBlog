import Link from "next/link";
import { AlertCircle, Home, LogIn } from "lucide-react";

export default function BannedPage() {
  return (
    <div className="bg-card border border-border rounded-lg shadow-lg p-8 md:p-12 text-center">
      {/* Icon */}
      <div className="mb-6 flex justify-center">
        <div className="relative">
          <div className="absolute inset-0 bg-destructive/20 rounded-full blur-xl"></div>
          <div className="relative bg-destructive/10 p-4 rounded-full">
            <AlertCircle
              className="w-12 h-12 text-destructive"
              strokeWidth={1.5}
            />
          </div>
        </div>
      </div>

      {/* Heading */}
      <h1 className="text-3xl md:text-4xl font-bold text-foreground mb-3">
        Account Banned
      </h1>

      {/* Subtitle */}
      <p className="text-muted-foreground text-base md:text-lg mb-2">
        Your account has been suspended
      </p>

      {/* Description */}
      <div className="bg-muted/50 border border-border rounded-md p-4 mb-8 text-left">
        <p className="text-muted-foreground text-sm leading-relaxed">
          Your account has been banned due to a violation of our Terms of
          Service. If you believe this is a mistake, please contact our support
          team for assistance.
        </p>
      </div>

      {/* Contact Info */}
      <div className="mb-8 p-4 bg-secondary/10 border border-secondary/20 rounded-md">
        <p className="text-sm text-muted-foreground mb-2">
          <span className="font-semibold text-foreground">Need help?</span>
        </p>
        <p className="text-sm text-muted-foreground">
          Contact us at{" "}
          <a
            href="https://mail.google.com/mail/?view=cm&fs=1&to=hainguyenhoang1205@gmail.com"
            target="_blank"
            rel="noopener noreferrer"
            className="text-primary hover:underline font-medium"
          >
            hainguyenhoang1205@gmail.com
          </a>
        </p>
      </div>

      {/* Action Buttons */}
      <div className="flex flex-col gap-3 sm:flex-row sm:gap-3">
        <Link
          href="/auth/login"
          className="flex-1 inline-flex items-center justify-center gap-2 px-6 py-3 bg-primary text-primary-foreground font-medium rounded-md hover:bg-primary/90 transition-colors duration-200"
        >
          <LogIn className="w-4 h-4" />
          <span>Login</span>
        </Link>
        <Link
          href="/"
          className="flex-1 inline-flex items-center justify-center gap-2 px-6 py-3 bg-secondary text-secondary-foreground font-medium rounded-md hover:bg-secondary/90 transition-colors duration-200"
        >
          <Home className="w-4 h-4" />
          <span>Home</span>
        </Link>
      </div>
    </div>
  );
}
