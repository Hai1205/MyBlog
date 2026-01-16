import Link from "next/link";
import React from "react";
import { Calendar } from "lucide-react";
import { Card } from "@/components/ui/card";
import { formatDateAgo } from "@/lib/utils";

interface BlogCardProps {
  blog: IBlog;
}

const BlogCard: React.FC<BlogCardProps> = ({ blog }) => {
  return (
    <Link href={`/blog/${blog.id}`}>
      <Card className="overflow-hidden rounded-lg shadow-none transition-shadow duration-300 hover:shadow-xl border-none">
        <div className="w-full h-50">
          <img src={blog.imageUrl} alt={blog.title} className="w-full h-full object-cover" />
        </div>

        <div className="p-0">
          <div>
            <p className="flex items-center justify-center gap-2 text-sm text-gray-500">
              <Calendar size={16} />
              <span>{formatDateAgo(blog.createdAt)}</span>
            </p>
            <h2 className="text-lg font-semibold mt-1 line-clamp-1 text-center">
              {blog.title}
            </h2>
            <p className="text-center">{blog.description.slice(0, 30)}...</p>
          </div>
        </div>
      </Card>
    </Link>
  );
};

export default BlogCard;
