"use client";

import Link from "next/link";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Home, Search, ShoppingBag, ArrowLeft } from "lucide-react";

export default function NotFoundClient() {
  return (
    <div className="min-h-screen bg-linear-to-br from-background to-muted">
      <div className="container mx-auto px-4 py-16">
        <div className="flex flex-col items-center justify-center min-h-[60vh] text-center">
          <Card className="w-full max-w-2xl">
            <CardContent className="p-12">
              {/* 404 Number */}
              <div className="mb-8">
                <h1 className="text-8xl md:text-9xl font-serif font-bold text-primary mb-4">
                  404
                </h1>

                <div className="w-24 h-1 bg-linear-to-br from-primary to-secondary mx-auto rounded-full"></div>
              </div>

              {/* Error Message */}
              <div className="mb-8">
                <h2 className="text-2xl md:text-3xl font-serif font-bold text-foreground mb-4">
                  Page Not Found
                </h2>

                <p className="text-muted-foreground text-lg mb-2">
                  Sorry, we couldn't find the page you're looking for.
                </p>

                <p className="text-muted-foreground">
                  The page may have been deleted, renamed, or temporarily
                  unavailable.
                </p>
              </div>

              {/* Action Buttons */}
              <div className="flex flex-col sm:flex-row gap-4 justify-center items-center">
                <Button asChild size="lg" className="w-full sm:w-auto">
                  <Link href="/" className="flex items-center gap-2">
                    <Home className="h-4 w-4" />
                    Go Home
                  </Link>
                </Button>

                <Button
                  asChild
                  variant="outline"
                  size="lg"
                  className="w-full sm:w-auto bg-transparent"
                >
                  <Link href="/blogs/new" className="flex items-center gap-2">
                    <ShoppingBag className="h-4 w-4" />
                    Create Blog
                  </Link>
                </Button>
              </div>

              {/* Additional Help */}
              <div className="mt-8 pt-8 border-t">
                <p className="text-sm text-muted-foreground mb-4">
                  Or you can try:
                </p>

                <div className="flex flex-wrap justify-center gap-4 text-sm">
                  <Link
                    href="/blogs"
                    className="text-primary hover:underline flex items-center gap-1"
                  >
                    <Search className="h-3 w-3" />
                    Browse Blogs
                  </Link>

                  <Link
                    href="/blogs/saved"
                    className="text-primary hover:underline"
                  >
                    Saved Blogs
                  </Link>

                  <Link
                    href="/my-blogs"
                    className="text-primary hover:underline"
                  >
                    My Blogs
                  </Link>
                </div>
              </div>
            </CardContent>
          </Card>

          {/* Back Button */}
          <Button
            variant="ghost"
            size="sm"
            onClick={() => window.history.back()}
            className="mt-6 text-muted-foreground hover:text-foreground"
          >
            <ArrowLeft className="h-4 w-4 mr-2" />
            Go Back
          </Button>
        </div>
      </div>
    </div>
  );
}
