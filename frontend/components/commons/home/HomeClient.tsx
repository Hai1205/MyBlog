import BlogsClient from "../blogs/BlogsClient";
import FeaturesSection from "./FeaturesSection";
import HeroSection from "./HeroSection";

export default function HomeClient() {
  return (
    <div className="flex flex-col">
      <HeroSection />
      <FeaturesSection />
      <BlogsClient />
    </div>
  );
}
