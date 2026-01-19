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
import { useState } from "react";
import { formatDateAgo } from "@/lib/utils";
import { useAuthStore } from "@/stores/authStore";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import { Skeleton } from "@/components/ui/skeleton";
import { useBlogQuery } from "@/hooks/api/queries/useBlogQueries";
import {
  useAddCommentMutation,
  useDeleteCommentMutation,
  useDeleteBlogMutation,
  useSaveBlogMutation,
  useUnsaveBlogMutation,
} from "@/hooks/api/mutations/useBlogMutations";

const BlogDetailClient = () => {
  const { userAuth } = useAuthStore();

  const router = useRouter();
  const { id } = useParams();

  const { data: blogResponse, isLoading } = useBlogQuery(id as string);
  const blog = blogResponse?.data?.blog;

  const { mutate: addCommentMutation } = useAddCommentMutation();
  const { mutate: deleteCommentMutation } = useDeleteCommentMutation();
  const { mutate: deleteBlogMutation } = useDeleteBlogMutation();
  const { mutate: saveBlogMutation } = useSaveBlogMutation();
  const { mutate: unsaveBlogMutation } = useUnsaveBlogMutation();

  const [content, setContent] = useState("");
  const [saved, setSaved] = useState(false);

  const handleAddComment = async () => {
    if (!id || !content.trim() || !userAuth?.id) return;

    addCommentMutation({
      blogId: id as string,
      userId: userAuth.id,
      content,
    });

    setContent("");
  };

  const handleDeleteComment = async (commentId: string) => {
    if (!commentId || !id) return;
    deleteCommentMutation({ commentId, blogId: id as string });
  };

  const handleDeleteBlog = async (blogId: string) => {
    if (!blogId) return;
    deleteBlogMutation(
      { blogId },
      {
        onSuccess: () => {
          router.push("/blogs");
        },
      },
    );
  };

  const handleSaveBlog = async (blogId: string) => {
    if (!blogId || !userAuth) return;

    if (!saved) {
      setSaved(true);
      saveBlogMutation({ blogId, userId: userAuth.id });
    } else {
      setSaved(false);
      unsaveBlogMutation({ blogId, userId: userAuth.id });
    }
  };

  if (isLoading) {
    return <BlogDetailSkeleton />;
  }

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

const BlogDetailSkeleton = () => {
  return (
    <div className="max-w-4xl mx-auto p-6 space-y-6">
      {/* Main Blog Card Skeleton */}
      <Card>
        <CardHeader>
          {/* Title Skeleton */}
          <Skeleton className="h-10 w-3/4 mb-4" />

          {/* Author Section Skeleton */}
          <div className="flex items-center gap-2 mt-2">
            <Skeleton className="w-8 h-8 rounded-full" />
            <Skeleton className="h-4 w-32" />
            <Skeleton className="h-10 w-10 rounded-md ml-3" />
          </div>
        </CardHeader>
        <CardContent className="space-y-4">
          {/* Thumbnail Skeleton */}
          <Skeleton className="w-full h-64 rounded-lg" />

          {/* Description Skeleton */}
          <div className="space-y-2">
            <Skeleton className="h-5 w-full" />
            <Skeleton className="h-5 w-5/6" />
          </div>

          {/* Content Skeleton */}
          <div className="space-y-3 mt-4">
            <Skeleton className="h-4 w-full" />
            <Skeleton className="h-4 w-full" />
            <Skeleton className="h-4 w-4/5" />
            <Skeleton className="h-4 w-full" />
            <Skeleton className="h-4 w-3/4" />
            <Skeleton className="h-4 w-full" />
            <Skeleton className="h-4 w-5/6" />
          </div>
        </CardContent>
      </Card>

      {/* Comment Input Card Skeleton */}
      <Card>
        <CardHeader>
          <Skeleton className="h-6 w-48" />
        </CardHeader>
        <CardContent>
          <Skeleton className="h-4 w-32 mb-2" />
          <div className="flex items-center gap-2">
            <Skeleton className="h-10 flex-1 rounded-md" />
            <Skeleton className="h-10 w-10 rounded-md shrink-0" />
          </div>
        </CardContent>
      </Card>

      {/* Comments List Card Skeleton */}
      <Card>
        <CardHeader>
          <Skeleton className="h-6 w-40" />
        </CardHeader>
        <CardContent className="space-y-4">
          {[1, 2, 3].map((i) => (
            <div key={i} className="border-b py-2 flex items-center gap-3">
              <div className="flex-1 space-y-2">
                <div className="flex items-center gap-2">
                  <Skeleton className="w-8 h-8 rounded-full" />
                  <Skeleton className="h-4 w-24" />
                </div>
                <Skeleton className="h-4 w-full" />
                <Skeleton className="h-3 w-20" />
              </div>
            </div>
          ))}
        </CardContent>
      </Card>
    </div>
  );
};

export default BlogDetailClient;
