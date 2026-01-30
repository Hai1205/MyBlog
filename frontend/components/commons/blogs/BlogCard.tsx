import Link from "next/link";
import React from "react";
import { Calendar, BookmarkX, Edit, Trash2, Copy } from "lucide-react";
import { Card } from "@/components/ui/card";
import { formatDateAgo } from "@/lib/utils";
import { Button } from "@/components/ui/button";

interface BlogCardProps {
  blog: IBlog;
  onUnsave?: (blogId: string) => void;
  onUpdate?: (blog: IBlog) => void;
  onDuplicate?: (blogId: string) => void;
  onDelete?: (blogId: string) => void;
}

export const BlogCard = ({
  blog,
  onUnsave,
  onUpdate,
  onDuplicate,
  onDelete,
}: BlogCardProps) => {
  const handleAction = (e: React.MouseEvent, action: () => void) => {
    e.preventDefault();
    e.stopPropagation();
    action();
  };

  return (
    <Link href={`/blogs/${blog.id}`}>
      <Card className="overflow-hidden rounded-lg shadow-none transition-all duration-300 hover:shadow-2xl hover:scale-105 hover:-translate-y-2 border border-gray-100 hover:border-primary/20 relative group">
        {/* Action buttons overlay */}
        {(onUnsave || onUpdate || onDelete) && (
          <div className="absolute top-2 right-2 z-10 flex gap-1 opacity-0 group-hover:opacity-100 transition-opacity duration-200">
            {onUnsave && (
              <Button
                size="icon"
                variant="secondary"
                className="h-8 w-8 bg-white/90 hover:bg-red-500 hover:text-white shadow-md"
                onClick={(e) => handleAction(e, () => onUnsave(blog.id))}
                title="Unsave blog"
              >
                <BookmarkX size={16} />
              </Button>
            )}

            {onUpdate && (
              <Button
                size="icon"
                variant="secondary"
                className="h-8 w-8 bg-white/90 hover:bg-blue-500 hover:text-white shadow-md"
                onClick={(e) => handleAction(e, () => onUpdate(blog))}
                title="Edit blog"
              >
                <Edit size={16} />
              </Button>
            )}

            {onDuplicate && (
              <Button
                size="icon"
                variant="secondary"
                className="h-8 w-8 bg-white/90 hover:bg-blue-500 hover:text-white shadow-md"
                onClick={(e) => handleAction(e, () => onDuplicate(blog.id))}
                title="Duplicate blog"
              >
                <Copy size={16} />
              </Button>
            )}

            {onDelete && (
              <Button
                size="icon"
                variant="secondary"
                className="h-8 w-8 bg-white/90 hover:bg-red-600 hover:text-white shadow-md"
                onClick={(e) => handleAction(e, () => onDelete(blog.id))}
                title="Delete blog"
              >
                <Trash2 size={16} />
              </Button>
            )}
          </div>
        )}

        <div className="w-full h-50 overflow-hidden">
          <img
            src={blog.thumbnailUrl || "/svgs/placeholder.svg"}
            alt={blog.title}
            className="w-full h-full object-cover transition-transform duration-300 group-hover:scale-110"
          />
        </div>

        <div className="p-0">
          <div>
            <p className="flex items-center justify-center gap-2 text-sm text-gray-500">
              <Calendar size={16} />
              <span>{formatDateAgo(blog.createdAt)}</span>
            </p>
            <h2 className="text-lg text-primary font-semibold mt-1 line-clamp-1 text-center">
              {blog.title}
            </h2>
            <p className="text-center text-secondary">
              {blog.description.slice(0, 30)}...
            </p>
          </div>
        </div>
      </Card>
    </Link>
  );
};
