"use client";

import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
  Bookmark,
  BookmarkCheck,
  Edit,
  Trash2,
  Trash2Icon,
  User2,
} from "lucide-react";
import Link from "next/link";
import { useParams, useRouter } from "next/navigation";
import { useCallback, useEffect, useState } from "react";
import { formatDateAgo } from "@/lib/utils";
import { useAuthStore } from "@/stores/authStore";
import { useBlogStore } from "@/stores/blogStore";
import Loading from "../layout/Loading";

const BlogDetailClient = () => {
  const { userAuth } = useAuthStore();
  const {
    isLoading,
    savedBlogs,
    getBlogComments,
    addComment,
    deleteComment,
    getBlog,
    deleteBlog,
    saveBlog,
  } = useBlogStore();

  //   const { userAuth, user, fetchBlogs, savedBlogs, getSavedBlogs } = useAppData();
  const router = useRouter();
  const { blogId } = useParams();
  const [blog, setBlog] = useState<IBlog | null>(null);
  //   const [author, setAuthor] = useState<IUser | null>(null);

  const [comments, setComments] = useState<IComment[]>([]);

  const handleGetBlogComments = useCallback(async () => {
    if (!blogId) return;

    const res = await getBlogComments(blogId as string);
    setComments(res?.data?.comments || []);
  }, [blogId]);

  useEffect(() => {
    handleGetBlogComments();
  }, [blogId]);

  const [content, setContent] = useState("");

  const handleAddComment = useCallback(async () => {
    if (!blogId) return;

    await addComment(blogId as string, userAuth?.id as string, content);
    setContent("");
  }, [blogId, userAuth?.id, content]);

  const handleGetBlog = useCallback(async () => {
    if (!blogId) return;

    const res = await getBlog(blogId as string);
    setBlog(res?.data?.blog || null);
  }, [blogId]);

  const handleDeleteComment = useCallback(
    async (commentId: string) => {
      if (!commentId) return;

      await deleteComment(commentId);
    },
    [deleteComment],
  );

  const handleDeleteBlog = useCallback(
    async (blogId: string) => {
      if (!blogId) return;

      await deleteBlog(blogId);
    },
    [blogId],
  );

  const [saved, setSaved] = useState(false);

  useEffect(() => {
    if (savedBlogs && savedBlogs.some((b) => b.id === blogId)) {
      setSaved(true);
    } else {
      setSaved(false);
    }
  }, [savedBlogs, blogId]);

  const handleSaveBlog = useCallback(
    async (blogId: string) => {
      if (!blogId && !userAuth) return;

      await saveBlog(blogId, userAuth?.id as string);
      setSaved(!saved);
    },
    [blogId, saved],
  );

  useEffect(() => {
    handleGetBlog();
  }, [blogId]);

  if (!blog) {
    return <Loading />;
  }

  return (
    <div className="max-w-4xl mx-auto p-6 space-y-6">
      <Card>
        <CardHeader>
          <h1 className="text-3xl font-bold text-gray-900">{blog.title}</h1>
          <p className="text-gray-600 mt-2 flex items-center">
            <Link
              className="flex items-center gap-2"
              href={`/profile/${blog?.author?.id}`}
            >
              <img
                src={blog?.author?.avatarUrl}
                className="w-8 h-8 rounded-full"
                alt=""
              />
              {blog?.author?.fullname}
            </Link>
            {userAuth && (
              <Button
                variant={"ghost"}
                className="mx-3"
                size={"lg"}
                disabled={isLoading}
                onClick={() => handleSaveBlog(blogId as string)}
              >
                {saved ? <BookmarkCheck /> : <Bookmark />}
              </Button>
            )}
            {blog.author.id === userAuth?.id && (
              <>
                <Button
                  size={"sm"}
                  onClick={() => router.push(`/blog/edit/${blogId}`)}
                >
                  <Edit />
                </Button>
                <Button
                  variant={"destructive"}
                  className="mx-2"
                  size={"sm"}
                  onClick={() => handleDeleteBlog(blogId as string)}
                  disabled={isLoading}
                >
                  <Trash2Icon />
                </Button>
              </>
            )}
          </p>
        </CardHeader>
        <CardContent>
          <img
            src={blog.thumbnailUrl}
            alt=""
            className="w-full h-64 object-cover rounded-lg mb-4"
          />
          <p className="text-lg text-gray-700 mb-4">{blog.description}</p>
          <div
            className="prose max-w-none"
            dangerouslySetInnerHTML={{ __html: blog.content }}
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
            <Input
              id="comment"
              placeholder="Type your comment here"
              className="my-2"
              value={content}
              onChange={(e) => setContent(e.target.value)}
            />
            <Button onClick={handleAddComment} disabled={isLoading}>
              {isLoading ? "Adding comment..." : "Post Comment"}
            </Button>
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
                      <span className="user border border-gray-400 rounded-full p-1">
                        <User2 />
                      </span>
                      {comment.fullname}
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
