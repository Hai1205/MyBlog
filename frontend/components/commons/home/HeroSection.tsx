"use client";

import Link from "next/link";
import { Button } from "@/components/ui/button";
import { FileText, Sparkles } from "lucide-react";
import { useRouter } from "next/navigation";
import Typewriter from "typewriter-effect";
import { useBlogStore } from "@/stores/blogStore";

export const HeroSection = () => {
  const { setBlogToEdit } = useBlogStore();

  const router = useRouter();

  const handleCreate = async () => {
    setBlogToEdit(null);
    router.push("/blogs/new");
  };

  return (
    <section className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 flex flex-col items-center justify-center gap-8 py-24 md:py-32">
      <div className="flex flex-col items-center gap-4 text-center">
        <h1 className="text-4xl font-bold tracking-tight sm:text-5xl md:text-6xl lg:text-7xl text-balance">
          Write Blogs With
          <Typewriter
            options={{
              strings: [
                "AI Assistant",
                "Creative Content",
                "Smart AI",
                "AI Technology",
              ],
              autoStart: true,
              loop: true,
              deleteSpeed: 50,
              wrapperClassName:
                "bg-gradient-to-br from-primary to-secondary bg-clip-text text-transparent",
            }}
          />
        </h1>
        <p className="max-w-175 text-lg text-muted-foreground text-balance md:text-xl leading-relaxed">
          Create and share professional articles with AI assistance. Get
          intelligent content suggestions and elevate your writing quality.
        </p>
      </div>

      <div className="flex flex-col gap-4 sm:flex-row">
        <Button size="lg" className="gap-2" onClick={handleCreate}>
          <Sparkles className="h-5 w-5" />
          Write Blog Now
        </Button>

        <Link href="/blogs/my-blogs">
          <Button size="lg" variant="outline" className="gap-2 bg-transparent">
            <FileText className="h-5 w-5" />
            My Blogs
          </Button>
        </Link>
      </div>
    </section>
  );
};
