"use client";

import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Switch } from "@/components/ui/switch";
import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import dynamic from "next/dynamic";
import { useParams, useRouter } from "next/navigation";
import { useBlogStore } from "@/stores/blogStore";
import { blogCategories } from "../admin/blogDashboard/constant";
import {
  RefreshCw,
  Upload,
  X,
  Image as ImageIcon,
  ChevronDown,
  ChevronUp,
} from "lucide-react";
import { ScrollArea } from "@/components/ui/scroll-area";
import DraggingOnPage from "../layout/Dragging/DraggingOnPage";
import { useToast } from "@/hooks/use-toast";
import { useAuthStore } from "@/stores/authStore";

const JoditEditor = dynamic(() => import("jodit-react"), { ssr: false });

interface EditBlogClientProps {
  isCreate?: boolean;
}

const EditBlogClient = ({ isCreate }: EditBlogClientProps) => {
  const {
    isLoading,
    getBlog,
    updateBlog,
    createBlog,
    analyzeTitle,
    analyzeDescription,
    analyzeContent,
  } = useBlogStore();
  const { userAuth } = useAuthStore();

  const { toast } = useToast();
  const router = useRouter();

  const [AIContentLoading, setAIContentLoading] = useState(false);
  const editor = useRef(null);
  const [content, setContent] = useState("");
  const [AITitleLoading, setAITitleLoading] = useState(false);
  const [AIDescriptionLoading, setAIDescriptionLoading] = useState(false);
  const [isDraggingOnThumbnail, setIsDraggingOnThumbnail] = useState(false);
  const [previewImage, setPreviewImage] = useState<string | null>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);
  const [isDetailsOpen, setIsDetailsOpen] = useState(true);
  //   const router = useRouter();

  const { id } = useParams();

  const [formData, setFormData] = useState<{
    title: string;
    description: string;
    category: string;
    thumbnail: File | null;
    content: string;
    isVisibility: boolean;
  }>({
    title: "",
    description: "",
    category: "technology",
    thumbnail: null,
    content: "",
    isVisibility: true,
  });

  const handleInputChange = (e: any) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleFileChange = (e: any) => {
    const file = e.target.files[0];
    if (file) {
      if (!file.type.startsWith("image/")) {
        toast({
          title: "Invalid file type",
          description: "Please select an image file",
          variant: "destructive",
        });
        return;
      }
      setFormData({ ...formData, thumbnail: file });

      // Create preview
      const reader = new FileReader();
      reader.onloadend = () => {
        setPreviewImage(reader.result as string);
      };
      reader.readAsDataURL(file);
    }
  };

  // Thumbnail drag and drop handlers
  const handleThumbnailDragEnter = (e: React.DragEvent<HTMLDivElement>) => {
    e.preventDefault();
    e.stopPropagation();
    if (e.dataTransfer.types.includes("Files")) {
      setIsDraggingOnThumbnail(true);
    }
  };

  const handleThumbnailDragLeave = (e: React.DragEvent<HTMLDivElement>) => {
    e.preventDefault();
    e.stopPropagation();
    if (e.currentTarget === e.target) {
      setIsDraggingOnThumbnail(false);
    }
  };

  const handleThumbnailDragOver = (e: React.DragEvent<HTMLDivElement>) => {
    e.preventDefault();
    e.stopPropagation();
  };

  const handleThumbnailDrop = (e: React.DragEvent<HTMLDivElement>) => {
    e.preventDefault();
    e.stopPropagation();
    setIsDraggingOnThumbnail(false);

    const file = e.dataTransfer.files?.[0];
    if (file && file.type.startsWith("image/")) {
      setFormData({ ...formData, thumbnail: file });

      // Create preview
      const reader = new FileReader();
      reader.onloadend = () => {
        setPreviewImage(reader.result as string);
      };
      reader.readAsDataURL(file);
    } else if (file) {
      toast({
        title: "Invalid file type",
        description: "Only image files are accepted!",
        variant: "destructive",
      });
    }
  };

  const handleRemoveImage = () => {
    setFormData({ ...formData, thumbnail: null });
    setPreviewImage(null);
    if (fileInputRef.current) {
      fileInputRef.current.value = "";
    }
  };

  const config = useMemo(
    () => ({
      readonly: false,
      placeholder: "Start typing...",
      style: {
        color: "#000000",
        background: "#ffffff",
      },
      contentCss: `
        body {
          color: #000000 !important;
          background: #ffffff !important;
          font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
          font-size: 16px;
          line-height: 1.6;
          padding: 16px;
        }
        p {
          color: #000000 !important;
          margin: 0 0 1em 0;
        }
        * {
          color: inherit !important;
        }
      `,
      minHeight: 500,
      maxHeight: 800,
      toolbarAdaptive: false,
      showCharsCounter: true,
      showWordsCounter: true,
      showXPathInStatusbar: false,
      uploader: {
        insertImageAsBase64URI: true,
        imagesExtensions: ["jpg", "png", "jpeg", "gif", "svg", "webp"],
      },
      filebrowser: {
        ajax: {
          url: "/api/upload",
        },
      },
      buttons: [
        "source",
        "|",
        "bold",
        "italic",
        "underline",
        "strikethrough",
        "|",
        "ul",
        "ol",
        "|",
        "outdent",
        "indent",
        "|",
        "font",
        "fontsize",
        "brush",
        "paragraph",
        "|",
        "image",
        "video",
        "table",
        "link",
        "|",
        "align",
        "undo",
        "redo",
        "|",
        "hr",
        "eraser",
        "copyformat",
        "|",
        "symbol",
        "fullsize",
        "print",
      ],
    }),
    [],
  );

  const [existingImage, setExistingImage] = useState<string | null>(null);

  const handleGetBlog = useCallback(async () => {
    if (!id) return;

    const res = await getBlog(id as string);
    const blogData = res?.data?.blog;
    if (!blogData) return;

    setFormData({
      title: blogData.title,
      description: blogData.description,
      category: blogData.category,
      thumbnail: null,
      content: blogData.content,
      isVisibility: blogData.isVisibility,
    });

    setContent(blogData.content || "");
    setExistingImage(blogData.thumbnailUrl || null);
  }, [id]);

  useEffect(() => {
    handleGetBlog();
  }, [id]);

  const handleSubmit = async (e: any) => {
    e.preventDefault();

    if (isCreate && userAuth) {
      const res = await createBlog(
        userAuth?.id,
        formData.title,
        formData.description,
        formData.category,
        formData.thumbnail,
        formData.content,
        formData.isVisibility,
      );

      if (res?.data?.success && res?.data?.blog) {
        router.push(`/blogs/${res.data.blog.id}`);
      }
    } else if (id) {
      await updateBlog(
        id as string,
        formData.title,
        formData.description,
        formData.category,
        formData.thumbnail,
        formData.content,
        formData.isVisibility,
      );

      toast({
        title: "Success",
        description: "Blog updated successfully",
      });
      router.push(`/blogs/${id}`);
    }
  };

  const handleAITitleResponse = async () => {
    setAITitleLoading(true);
    const res = await analyzeTitle(formData.title);
    setFormData({ ...formData, title: res?.data?.title || "" });
    setAITitleLoading(false);
  };

  const handleAIDescriptionResponse = async () => {
    setAIDescriptionLoading(true);
    const res = await analyzeDescription(formData.title, formData.description);
    setFormData({ ...formData, description: res?.data?.description || "" });
    setAIDescriptionLoading(false);
  };

  const handleAIBlogResponse = async () => {
    setAIContentLoading(true);
    const res = await analyzeContent(formData.content);
    setContent(res?.data?.content || "");
    setFormData({ ...formData, content: res?.data?.content || "" });
    setAIContentLoading(false);
  };

  return (
    <div className="w-full min-h-screen p-6 bg-linear-to-br from-background to-muted/20">
      <div className="max-w-7xl mx-auto">
        <div className="mb-6">
          <h2 className="text-3xl font-bold bg-linear-to-r from-primary to-primary/60 bg-clip-text text-transparent">
            {isCreate ? "Add New Blog" : "Edit Blog"}
          </h2>
          <p className="text-muted-foreground mt-1">
            {isCreate
              ? "Create and publish your new blog post"
              : "Update your blog post"}
          </p>
        </div>

        <form onSubmit={handleSubmit}>
          <div
            className={`grid grid-cols-1 gap-6 transition-all duration-500 ${
              isDetailsOpen ? "lg:grid-cols-5" : "lg:grid-cols-1"
            }`}
          >
            {/* Left Panel - Smaller */}
            {isDetailsOpen && (
              <div className="lg:col-span-2 space-y-6 animate-in fade-in slide-in-from-left duration-500">
                <Card className="shadow-lg border-2 hover:border-primary/50 transition-all duration-300">
                  <CardHeader
                    className="pb-4 cursor-pointer"
                    onClick={() => setIsDetailsOpen(!isDetailsOpen)}
                  >
                    <div className="flex items-center justify-between">
                      <h3 className="text-lg font-semibold flex items-center gap-2">
                        <span className="w-1.5 h-6 bg-primary rounded-full"></span>
                        Blog Details
                      </h3>
                      <Button
                        type="button"
                        variant="ghost"
                        size="icon"
                        className="h-8 w-8 rounded-full hover:bg-primary/10"
                      >
                        <ChevronUp
                          size={20}
                          className="text-muted-foreground"
                        />
                      </Button>
                    </div>
                  </CardHeader>
                  {isDetailsOpen && (
                    <CardContent className="space-y-5 animate-in fade-in slide-in-from-top-2 duration-300">
                      {/* Title */}
                      <div className="space-y-2">
                        <Label className="text-sm font-medium">Title</Label>
                        <div className="flex gap-2">
                          <Input
                            name="title"
                            value={formData.title}
                            onChange={handleInputChange}
                            placeholder="Enter blog title"
                            className={
                              AITitleLoading
                                ? "animate-pulse placeholder:opacity-60"
                                : ""
                            }
                            required
                          />
                          {isCreate && formData.title === "" ? null : (
                            <Button
                              type="button"
                              onClick={handleAITitleResponse}
                              disabled={AITitleLoading}
                              size="icon"
                              variant="outline"
                              className="shrink-0"
                            >
                              <RefreshCw
                                className={AITitleLoading ? "animate-spin" : ""}
                                size={18}
                              />
                            </Button>
                          )}
                        </div>
                      </div>

                      {/* Description */}
                      <div className="space-y-2">
                        <div className="flex items-center justify-between">
                          <Label
                            htmlFor="description"
                            className="text-sm font-medium"
                          >
                            Description
                          </Label>
                          <span className="text-xs text-muted-foreground">
                            {formData.description?.length || 0}/2000
                          </span>
                        </div>
                        <ScrollArea className="h-40 w-full border border-border rounded-md bg-background/50">
                          <textarea
                            id="description"
                            name="description"
                            value={formData.description || ""}
                            onChange={handleInputChange}
                            placeholder="Enter a brief description..."
                            className={`w-full min-h-40 px-3 py-2 text-sm bg-transparent focus:outline-none transition-colors resize-none border-0 ${
                              AIDescriptionLoading
                                ? "animate-pulse placeholder:opacity-60"
                                : ""
                            }`}
                            style={{ height: "auto" }}
                            onInput={(e) => {
                              e.currentTarget.style.height = "auto";
                              e.currentTarget.style.height =
                                e.currentTarget.scrollHeight + "px";
                            }}
                          />
                        </ScrollArea>
                        {isCreate && formData.description === "" ? null : (
                          <Button
                            onClick={handleAIDescriptionResponse}
                            type="button"
                            disabled={AIDescriptionLoading}
                            size="sm"
                            variant="outline"
                            className="w-full"
                          >
                            <RefreshCw
                              className={
                                AIDescriptionLoading ? "animate-spin" : ""
                              }
                              size={16}
                            />
                            <span className="ml-2">Generate with AI</span>
                          </Button>
                        )}
                      </div>

                      {/* Category */}
                      <div className="space-y-2">
                        <Label className="text-sm font-medium">Category</Label>
                        <Select
                          value={formData.category}
                          onValueChange={(value: any) =>
                            setFormData({ ...formData, category: value })
                          }
                        >
                          <SelectTrigger>
                            <SelectValue
                              placeholder={
                                formData.category || "Select category"
                              }
                            />
                          </SelectTrigger>
                          <SelectContent>
                            {blogCategories?.map((e, i) => (
                              <SelectItem key={i} value={e.value}>
                                {e.label}
                              </SelectItem>
                            ))}
                          </SelectContent>
                        </Select>
                      </div>

                      {/* Visibility Toggle */}
                      <div className="flex items-center justify-between rounded-lg border border-border p-4">
                        <div className="space-y-0.5">
                          <Label
                            htmlFor="visibility-toggle"
                            className="text-base"
                          >
                            Visibility Mode
                          </Label>
                        </div>
                        <Switch
                          id="visibility-toggle"
                          checked={formData.isVisibility || false}
                          onCheckedChange={(checked) =>
                            setFormData({ ...formData, isVisibility: checked })
                          }
                        />
                      </div>

                      {/* Thumbnail */}
                      <div className="space-y-2">
                        <Label className="text-sm font-medium">Thumbnail</Label>

                        {/* Preview or Upload Area */}
                        <div
                          onDragEnter={handleThumbnailDragEnter}
                          onDragLeave={handleThumbnailDragLeave}
                          onDragOver={handleThumbnailDragOver}
                          onDrop={handleThumbnailDrop}
                          onClick={() => fileInputRef.current?.click()}
                          className={`
                        relative w-full aspect-video rounded-lg overflow-hidden border-2 transition-all duration-300 cursor-pointer
                        ${
                          isDraggingOnThumbnail
                            ? "border-primary bg-primary/10 scale-[1.02]"
                            : previewImage || existingImage
                              ? "border-border hover:border-primary/50"
                              : "border-dashed border-border hover:border-primary hover:bg-primary/5"
                        }
                      `}
                        >
                          {previewImage ||
                          (!isCreate &&
                            existingImage &&
                            !formData.thumbnail) ? (
                            <>
                              <img
                                src={previewImage || existingImage || ""}
                                className="w-full h-full object-cover"
                                alt="Thumbnail preview"
                              />
                              <div className="absolute inset-0 bg-black/40 opacity-0 hover:opacity-100 transition-opacity duration-300 flex items-center justify-center gap-2">
                                <Button
                                  type="button"
                                  size="sm"
                                  variant="secondary"
                                  onClick={(e) => {
                                    e.stopPropagation();
                                    fileInputRef.current?.click();
                                  }}
                                >
                                  <Upload size={16} />
                                  <span className="ml-2">Change</span>
                                </Button>
                                {previewImage && (
                                  <Button
                                    type="button"
                                    size="sm"
                                    variant="destructive"
                                    onClick={(e) => {
                                      e.stopPropagation();
                                      handleRemoveImage();
                                    }}
                                  >
                                    <X size={16} />
                                    <span className="ml-2">Remove</span>
                                  </Button>
                                )}
                              </div>
                            </>
                          ) : (
                            <div className="w-full h-full flex flex-col items-center justify-center gap-3 p-4">
                              <div className="w-16 h-16 rounded-full bg-primary/10 flex items-center justify-center">
                                {isDraggingOnThumbnail ? (
                                  <Upload className="w-8 h-8 text-primary animate-bounce" />
                                ) : (
                                  <ImageIcon className="w-8 h-8 text-primary" />
                                )}
                              </div>
                              <div className="text-center">
                                <p className="text-sm font-medium text-foreground">
                                  {isDraggingOnThumbnail
                                    ? "Drop image here"
                                    : "Click to upload or drag & drop"}
                                </p>
                                <p className="text-xs text-muted-foreground mt-1">
                                  PNG, JPG, GIF up to 10MB
                                </p>
                              </div>
                            </div>
                          )}
                        </div>

                        <input
                          ref={fileInputRef}
                          type="file"
                          accept="image/*"
                          onChange={handleFileChange}
                          className="hidden"
                        />
                        <p className="text-xs text-muted-foreground">
                          Recommended: 1200x630px (16:9 ratio)
                        </p>
                      </div>
                    </CardContent>
                  )}
                </Card>

                {/* Submit Button - Below left panel */}
                <Button
                  type="submit"
                  className="w-full h-12 text-lg font-semibold shadow-lg hover:shadow-xl transition-all duration-300"
                  disabled={isLoading}
                >
                  {isLoading
                    ? isCreate
                      ? "Creating..."
                      : "Updating..."
                    : isCreate
                      ? "Create Blog"
                      : "Update Blog"}
                </Button>
              </div>
            )}

            {/* Right Panel - Larger */}
            <div
              className={`transition-all duration-500 ${
                isDetailsOpen ? "lg:col-span-3" : "lg:col-span-1"
              }`}
            >
              <Card
                className="shadow-lg border-2 hover:border-primary/50 transition-all duration-300 flex flex-col"
                style={{ minHeight: "calc(100vh - 200px)" }}
              >
                <CardHeader className="pb-4 shrink-0">
                  <div className="flex items-center justify-between">
                    <div>
                      <h3 className="text-lg font-semibold flex items-center gap-2">
                        <span className="w-1.5 h-6 bg-primary rounded-full"></span>
                        Blog Content
                      </h3>
                      <p className="text-sm text-muted-foreground mt-1">
                        Write your blog content with rich text formatting
                      </p>
                    </div>
                    <div className="flex items-center gap-2">
                      {!isDetailsOpen && (
                        <Button
                          type="button"
                          variant="outline"
                          size="sm"
                          onClick={() => setIsDetailsOpen(true)}
                          className="flex items-center gap-2"
                        >
                          <ChevronDown size={16} />
                          <span>Show Details</span>
                        </Button>
                      )}
                      {isCreate && (
                        <Button
                          type="button"
                          size="sm"
                          onClick={handleAIBlogResponse}
                          disabled={AIContentLoading}
                          variant="outline"
                        >
                          <RefreshCw
                            size={16}
                            className={AIContentLoading ? "animate-spin" : ""}
                          />
                          <span className="ml-2">Fix Grammar</span>
                        </Button>
                      )}
                    </div>
                  </div>
                </CardHeader>
                <CardContent className="flex-1 flex flex-col">
                  <div
                    className="flex-1 bg-white rounded-md overflow-hidden"
                    style={{ minHeight: "500px" }}
                  >
                    <JoditEditor
                      ref={editor}
                      value={content}
                      config={config}
                      tabIndex={1}
                      onBlur={(newContent: string) => {
                        setContent(newContent);
                        setFormData({ ...formData, content: newContent });
                      }}
                    />
                  </div>
                </CardContent>
              </Card>
            </div>
          </div>

          {/* Floating Submit Button - Show when details panel is hidden */}
          {!isDetailsOpen && (
            <div className="fixed bottom-6 right-6 z-40 animate-in fade-in slide-in-from-bottom duration-500">
              <Button
                type="submit"
                className="h-14 px-8 text-lg font-semibold shadow-2xl hover:shadow-3xl transition-all duration-300 rounded-full"
                disabled={isLoading}
              >
                {isLoading
                  ? isCreate
                    ? "Creating..."
                    : "Updating..."
                  : isCreate
                    ? "Create Blog"
                    : "Update Blog"}
              </Button>
            </div>
          )}
        </form>
      </div>

      {/* Dragging Overlay */}
      {isDraggingOnThumbnail && (
        <DraggingOnPage
          title="Drop your image here"
          subtitle="Release to upload thumbnail"
        />
      )}
    </div>
  );
};

export default EditBlogClient;
