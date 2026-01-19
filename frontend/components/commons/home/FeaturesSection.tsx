"use client";

import { CheckCircle, Sparkles, Zap } from "lucide-react";
import { useEffect, useState } from "react";

export const FeaturesSection = () => {
  const [isVisible, setIsVisible] = useState(false);
  useEffect(() => setIsVisible(true), []);

  const features = [
    {
      icon: Sparkles,
      title: "Smart AI Assistant",
      description:
        "Get intelligent content suggestions from AI to write professional blogs. Automatically generate engaging titles, improve writing style, and optimize SEO.",
      color: "primary",
      delay: "100ms",
    },
    {
      icon: Zap,
      title: "Write & Publish Fast",
      description:
        "Modern editor interface with markdown support. Publish articles in just a few clicks and share with the community.",
      color: "secondary",
      delay: "200ms",
    },
    {
      icon: CheckCircle,
      title: "Article Management",
      description:
        "Create and manage multiple blogs easily. Track views, interactions, and analyze your content performance.",
      color: "accent",
      delay: "300ms",
    },
  ];

  return (
    <section className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-24 md:py-32">
      <div className="grid gap-8 md:grid-cols-3">
        {features.map(({ icon: Icon, title, description, color, delay }, i) => (
          <div
            key={i}
            className={`group flex flex-col gap-4 rounded-lg border border-border bg-card p-6 transition-all duration-500 hover:shadow-xl hover:scale-105 hover:-translate-y-2 ${
              isVisible
                ? "opacity-100 translate-y-0"
                : "opacity-0 translate-y-8"
            }`}
            style={{ transitionDelay: delay }}
          >
            <div
              className={`flex h-12 w-12 items-center justify-center rounded-lg bg-${color}/10 transition-all duration-300 group-hover:bg-${color}/20 group-hover:rotate-6 group-hover:scale-110`}
            >
              <Icon className={`h-6 w-6 text-${color}`} />
            </div>
            <h3
              className={`text-xl font-semibold transition-colors duration-300 group-hover:text-${color}`}
            >
              {title}
            </h3>
            <p className="text-muted-foreground leading-relaxed">
              {description}
            </p>
          </div>
        ))}
      </div>
    </section>
  );
};
