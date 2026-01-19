"use client";

import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
  Bookmark,
  BookmarkCheck,
  Edit,
  Send,
  Trash2,
  Trash2Icon,
  User2,
} from "lucide-react";
import Link from "next/link";
import { useParams, useRouter } from "next/navigation";
import { useEffect, useState } from "react";
import { formatDateAgo } from "@/lib/utils";
import { useAuthStore } from "@/stores/authStore";
import { useBlogStore } from "@/stores/blogStore";
import Loading from "../layout/Loading";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";

const BlogDetailClient = () => {
  const { userAuth } = useAuthStore();
  const {
    addComment,
    deleteComment,
    getBlog,
    deleteBlog,
    saveBlog,
    unsaveBlog,
  } = useBlogStore();

  const router = useRouter();
  const { id } = useParams();
  const [blog, setBlog] = useState<IBlog | null>(null);
  const [content, setContent] = useState("");
  const [isLoading, setIsLoading] = useState(false);

  // Fetch blog with author and comments in one call
  useEffect(() => {
    const fetchBlog = async () => {
      setIsLoading(true);
      if (!id) return;
      const res = await getBlog(id as string);
      console.log("Fetched blog data:", res);
      const blogData = res?.data?.blog;

      if (blogData) {
        setBlog(blogData);
      }

      setIsLoading(false);
    };
    fetchBlog();
  }, [id, getBlog]);

  const handleAddComment = async () => {
    if (!id || !content.trim()) return;

    const res = await addComment(id as string, userAuth?.id as string, content);

    setContent("");

    const newComment = res?.data?.comment;

    if (!newComment) return;

    // Add new comment to the list
    setBlog((prev) =>
      prev
        ? {
            ...prev,
            comments: [...(prev.comments || []), newComment],
          }
        : null,
    );
  };

  const handleDeleteComment = async (commentId: string) => {
    if (!commentId) return;

    setBlog((prev) =>
      prev
        ? {
            ...prev,
            comments: prev.comments?.filter((c) => c.id !== commentId) || [],
          }
        : null,
    );

    await deleteComment(commentId);
  };

  const handleDeleteBlog = async (blogId: string) => {
    if (!blogId) return;
    await deleteBlog(blogId);
    router.push("/blogs");
  };

  const [saved, setSaved] = useState(false);

  const handleSaveBlog = async (blogId: string) => {
    if (!blogId || !userAuth) return;

    if (!saved) {
      setSaved(true);
      await saveBlog(blogId, userAuth.id);
    } else {
      setSaved(false);
      await unsaveBlog(blogId, userAuth.id);
    }
  };

  if (isLoading) {
    return <Loading />;
  }

  console.log("Blog data:", blog);

  return (
    <div className="max-w-4xl mx-auto p-6 space-y-6">
      <Card>
        <CardHeader>
          <h1 className="text-3xl font-bold text-primary">{blog?.title}</h1>
          <div className="text-gray-600 mt-2 flex items-center">
            <Link
              className="flex items-center gap-2"
              href={`/profile/${blog?.author?.id}`}
            >
              <Avatar className="w-8 h-8 border-4 border-primary/20 shadow-md">
                {blog?.author?.avatarUrl && (
                  <AvatarImage
                    src={blog?.author?.avatarUrl}
                    alt={blog?.author?.username || "User"}
                    className="object-cover"
                  />
                )}
                <AvatarFallback className="bg-linear-to-br from-primary to-secondary text-primary-foreground font-bold text-sm">
                  {blog?.author?.username?.charAt(0).toUpperCase() || "U"}
                </AvatarFallback>
              </Avatar>

              <span className="text-sm font-medium">
                {blog?.author?.username}
              </span>
            </Link>

            {userAuth && (
              <Button
                variant={"ghost"}
                className="mx-3"
                size={"lg"}
                disabled={isLoading}
                onClick={() => handleSaveBlog(id as string)}
              >
                {saved ? <BookmarkCheck /> : <Bookmark />}
              </Button>
            )}
            {blog?.author?.id === userAuth?.id && (
              <>
                <Button
                  size={"sm"}
                  onClick={() => router.push(`/blogs/edit/${id}`)}
                >
                  <Edit />
                </Button>
                <Button
                  variant={"destructive"}
                  className="mx-2"
                  size={"sm"}
                  onClick={() => handleDeleteBlog(id as string)}
                  disabled={isLoading}
                >
                  <Trash2Icon />
                </Button>
              </>
            )}
          </div>
        </CardHeader>
        <CardContent>
          <img
            src={blog?.thumbnailUrl}
            alt=""
            className="w-full h-64 object-cover rounded-lg mb-4"
          />
          <p className="text-lg text-secondary font-medium mb-4">
            {blog?.description}
          </p>
          <div
            className="prose max-w-none"
            dangerouslySetInnerHTML={{ __html: blog?.content || "" }}
          />
        </CardContent>
      </Card>

      {userAuth && (
        <Card>
          <CardHeader>
            <h3 className="text-xl font-semibold">Leave a comment</h3>
          </CardHeader>
          <CardContent>
            <Label htmlFor="comment">Your Comment</Label>
            <div className="flex items-center gap-2 mt-2">
              <Input
                id="comment"
                placeholder="Type your comment here"
                value={content}
                onChange={(e) => setContent(e.target.value)}
                onKeyDown={(e) => {
                  if (e.key === "Enter" && !e.shiftKey) {
                    e.preventDefault();
                    handleAddComment();
                  }
                }}
              />
              <Button
                onClick={handleAddComment}
                disabled={isLoading || !content.trim()}
                size="icon"
                className="shrink-0"
              >
                <Send className="h-4 w-4" />
              </Button>
            </div>
          </CardContent>
        </Card>
      )}

      <Card>
        <CardHeader>
          <h3 className="text-lg font-medium">All Comments</h3>
        </CardHeader>
        <CardContent>
          {blog?.comments && blog.comments.length > 0 ? (
            blog.comments.map((comment, i) => {
              return (
                <div key={i} className="border-b py-2 flex items-center gap-3">
                  <div>
                    <p className="font-semibold flex items-center gap-1">
                      <span className="user border border-gray-400 rounded-full p-1">
                        <User2 />
                      </span>
                      {comment.username}
                    </p>
                    <p>{comment.content}</p>
                    <p className="text-xs text-gray-500">
                      {formatDateAgo(comment.createdAt)}
                    </p>
                  </div>
                  {comment.userId === userAuth?.id && (
                    <Button
                      onClick={() => handleDeleteComment(comment.id)}
                      variant={"destructive"}
                      disabled={isLoading}
                    >
                      <Trash2 />
                    </Button>
                  )}
                </div>
              );
            })
          ) : (
            <p>No Comments Yet</p>
          )}
        </CardContent>
      </Card>
    </div>
  );
};

export default BlogDetailClient;
