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
} from "lucide-react";
import Link from "next/link";
import { useParams, useRouter } from "next/navigation";
import { useState, useEffect } from "react";
import { formatDateAgo } from "@/lib/utils";
import { useAuthStore } from "@/stores/authStore";
import { useBlogStore } from "@/stores/blogStore";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import { ConfirmationDialog } from "@/components/commons/layout/ConfirmationDialog";
import {
  useBlogQuery,
  useSavedBlogsQuery,
} from "@/hooks/api/queries/useBlogQueries";
import {
  useAddCommentMutation,
  useDeleteCommentMutation,
  useDeleteBlogMutation,
  useSaveBlogMutation,
  useUnsaveBlogMutation,
} from "@/hooks/api/mutations/useBlogMutations";
import { BlogDetailSkeleton } from "./BlogDetailSkeleton";

const BlogDetailClient = () => {
  const { userAuth } = useAuthStore();
  const {
    commentsByBlogId,
    setCommentsForBlog,
    addCommentToBlog,
    removeCommentFromBlog,
    setBlogToEdit,
    setSavedBlogs,
    removeFromSavedBlogs,
    addToSavedBlogs,
  } = useBlogStore();

  const router = useRouter();
  const { id } = useParams();

  const { data: blogResponse, isLoading } = useBlogQuery(
    id as string,
    userAuth?.id as string,
  );
  const blog = blogResponse?.data?.blog;

  const { data: savedBlogsResponse } = useSavedBlogsQuery(userAuth?.id || "");

  useEffect(() => {
    const savedBlogs = savedBlogsResponse?.data?.blogs;
    setSavedBlogs(savedBlogs || []);
  }, [savedBlogsResponse?.data?.blogs, setSavedBlogs]);

  useEffect(() => {
    if (blog?.comments && id) {
      setCommentsForBlog(id as string, blog.comments);
    }
  }, [blog?.comments, id, setCommentsForBlog]);

  useEffect(() => {
    if (blog && userAuth) {
      const isSaved = blog.isSaved || false;
      setSaved(isSaved);
      console.log(isSaved);
    }
  }, [blog, userAuth]);

  const comments = id ? commentsByBlogId[id as string] || [] : [];

  const { mutateAsync: addCommentAsync } = useAddCommentMutation();
  const { mutateAsync: deleteCommentAsync } = useDeleteCommentMutation();
  const { mutateAsync: deleteBlogAsync } = useDeleteBlogMutation();
  const { mutateAsync: saveBlogAsync } = useSaveBlogMutation();
  const { mutateAsync: unsaveBlogAsync } = useUnsaveBlogMutation();

  const [content, setContent] = useState("");
  const [saved, setSaved] = useState(false);
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [commentToDelete, setCommentToDelete] = useState<string | null>(null);

  const handleAddComment = async () => {
    if (!id || !content.trim() || !userAuth?.id) return;

    addCommentAsync(
      {
        blogId: id as string,
        userId: userAuth.id,
        data: { content: content.trim() },
      },
      {
        onSuccess: (response) => {
          const newComment = response?.data?.comment;
          if (newComment && id) {
            addCommentToBlog(id as string, newComment);
          }
        },
      },
    );

    setContent("");
  };

  const handleUpdate = async (blog: IBlog) => {
    setBlogToEdit(blog);
    router.push(`/blogs/edit/${blog.id}`);
  };

  const handleDeleteComment = async (commentId: string) => {
    setCommentToDelete(commentId);
    setDeleteDialogOpen(true);
  };

  const confirmDeleteComment = async () => {
    if (!commentToDelete || !id) return;
    deleteCommentAsync(
      { commentId: commentToDelete, blogId: id as string },
      {
        onSuccess: () => {
          if (id) {
            removeCommentFromBlog(id as string, commentToDelete);
          }
          setDeleteDialogOpen(false);
          setCommentToDelete(null);
        },
      },
    );
  };

  const handleDeleteBlog = async (blogId: string) => {
    if (!blogId) return;
    deleteBlogAsync(
      { blogId },
      {
        onSuccess: () => {
          router.push("/blogs");
        },
      },
    );
  };

  const saveBlog = async (blogId: string) => {
    if (!blogId || !userAuth) return;

    if (!saved) {
      setSaved(true);
      saveBlogAsync({ blogId, userId: userAuth.id });
      addToSavedBlogs(blog as IBlog);
    } else {
      setSaved(false);
      unsaveBlogAsync({ blogId, userId: userAuth.id });
      removeFromSavedBlogs(blogId);
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

              <span className="text-sm font-medium text-secondary">
                {blog?.author?.username || "Unknown Author"}
              </span>
            </Link>

            {userAuth && (
              <Button
                variant={"ghost"}
                className="mx-3"
                size={"lg"}
                disabled={isLoading}
                onClick={() => saveBlog(id as string)}
              >
                {saved ? (
                  <BookmarkCheck className="text-primary" />
                ) : (
                  <Bookmark />
                )}
              </Button>
            )}
            {blog?.author?.id === userAuth?.id && (
              <>
                <Button size={"sm"} onClick={() => handleUpdate(blog as IBlog)}>
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
          {blog?.thumbnailUrl && (
            <img
              src={blog?.thumbnailUrl}
              alt=""
              className="w-full h-64 object-cover rounded-lg mb-4"
            />
          )}
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
          {comments && comments.length > 0 ? (
            comments.map((comment, i) => {
              return (
                <div key={i} className="border-b py-2 flex items-center gap-3">
                  <div>
                    <p className="font-semibold flex items-center gap-1">
                      <Avatar className="border-2 border-primary/20 shadow-md">
                        <AvatarFallback className="bg-linear-to-br from-primary to-secondary text-primary-foreground font-bold text-sm">
                          {comment.username?.charAt(0).toUpperCase() || "U"}
                        </AvatarFallback>
                      </Avatar>

                      <span className="text-sm font-medium text-secondary">
                        {comment.username || "Unknown User"}
                      </span>
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

      <ConfirmationDialog
        open={deleteDialogOpen}
        onOpenChange={setDeleteDialogOpen}
        onConfirm={confirmDeleteComment}
        title="Delete Comment"
        description="Are you sure you want to delete this comment? This action cannot be undone."
        confirmText="Delete"
        cancelText="Cancel"
        isDestructive
      />
    </div>
  );
};

export default BlogDetailClient;
