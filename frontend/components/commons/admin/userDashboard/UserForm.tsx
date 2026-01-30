"use client";

import { ChangeEvent, useState } from "react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { ScrollArea } from "@/components/ui/scroll-area";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Eye, EyeOff, Lock, Save, UserIcon } from "lucide-react";
import { EUserRole, EUserStatus } from "@/types/enum";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import {
  ExtendedUserData,
  roleSelection,
  statusSelection,
} from "./UserDashboardClient";

interface UserFormProps {
  data: ExtendedUserData | null;
  onChange: (field: keyof ExtendedUserData, value: string) => void;
  previewAvatar: string;
  handleAvatarChange: (e: ChangeEvent<HTMLInputElement>) => void;
  isCreateDialog: boolean;
  showFooterButtons?: boolean;
}

export const UserForm = ({
  data,
  onChange,
  previewAvatar,
  handleAvatarChange,
  isCreateDialog,
}: UserFormProps) => {
  const [showPassword, setShowPassword] = useState(false);
  return (
    <div className="space-y-6 pr-2">
      {/* Avatar */}
      <div className="flex items-center justify-center">
        <div className="relative w-32 h-32 rounded-full overflow-hidden border-4 border-primary/20 shadow-lg hover:border-primary/40 transition-all duration-300">
          <Avatar className="w-full h-full">
            <AvatarImage
              src={
                previewAvatar ||
                (data as any)?.avatar ||
                "/svgs/placeholder.svg"
              }
              alt={data?.username || "User"}
              className="object-cover"
            />
            <AvatarFallback className="bg-linear-to-br from-primary/20 to-secondary/20">
              <UserIcon className="w-12 h-12 text-muted-foreground" />
            </AvatarFallback>
          </Avatar>

          <div className="absolute inset-0 bg-black/60 opacity-0 hover:opacity-100 flex items-center justify-center transition-all duration-300 cursor-pointer">
            <Button
              variant="secondary"
              size="sm"
              className="bg-linear-to-br from-primary to-secondary hover:from-primary/90 hover:to-secondary/90 text-primary-foreground shadow-lg"
              onClick={() =>
                document.getElementById("avatar-input-create")?.click()
              }
            >
              Choose Image
            </Button>
          </div>

          <input
            id="avatar-input-create"
            type="file"
            accept="image/*"
            className="hidden"
            onChange={handleAvatarChange}
          />
        </div>
      </div>

      {/* Username */}
      <div className="space-y-2">
        <Label htmlFor="form-username" className="text-sm font-medium">
          Username <span className="text-destructive">*</span>
        </Label>
        <Input
          id="form-username"
          value={data?.username || ""}
          onChange={(e) => onChange("username", e.target.value)}
          className="bg-background/50 border-border/50 focus:border-primary transition-colors"
          placeholder="Enter username"
          required
        />
      </div>

      {/* Email - only for create */}
      {isCreateDialog && (
        <div className="space-y-2">
          <Label htmlFor="form-email" className="text-sm font-medium">
            Email <span className="text-destructive">*</span>
          </Label>
          <Input
            id="form-email"
            type="email"
            value={data?.email || ""}
            onChange={(e) => onChange("email", e.target.value)}
            className="bg-background/50 border-border/50 focus:border-primary transition-colors"
            placeholder="example@vietau.com"
            required
          />
        </div>
      )}

      {/* Password - only for create */}
      {isCreateDialog && (
        <div className="space-y-2">
          <Label htmlFor="form-password" className="text-sm font-medium">
            Password <span className="text-destructive">*</span>
          </Label>
          <div className="relative">
            <Lock className="absolute left-3 top-2.5 h-4 w-4 text-muted-foreground" />
            <Input
              id="form-password"
              name="password"
              type={showPassword ? "text" : "password"}
              value={data?.password || ""}
              onChange={(e) => onChange("password", e.target.value)}
              className="pl-10 bg-background/50 border-border/50 focus:border-primary transition-colors"
              placeholder="Enter password"
              required
            />
            <button
              type="button"
              onClick={() => setShowPassword(!showPassword)}
              className="absolute right-3 top-2.5 text-muted-foreground hover:text-foreground"
            >
              {showPassword ? (
                <EyeOff className="h-4 w-4" />
              ) : (
                <Eye className="h-4 w-4" />
              )}
            </button>
          </div>
        </div>
      )}

      {/* Birth */}
      <div className="space-y-2">
        <Label htmlFor="form-birth" className="text-sm font-medium">
          Birth
        </Label>
        <Input
          id="form-birth"
          type="date"
          value={data?.birth || ""}
          onChange={(e) => onChange("birth", e.target.value)}
          className="bg-background/50 border-border/50 focus:border-primary transition-colors"
        />
      </div>

      {/* Summary */}
      <div className="space-y-2">
        <div className="flex items-center justify-between">
          <Label htmlFor="form-summary" className="text-sm font-medium">
            Summary
          </Label>
          <span className="text-xs text-muted-foreground">
            {data?.summary?.length || 0}/2000
          </span>
        </div>
        <ScrollArea className="h-30 w-full rounded-md border border-border/50 bg-background/50">
          <textarea
            id="form-summary"
            value={data?.summary || ""}
            onChange={(e) => {
              const value = e.target.value;
              const truncatedValue =
                value.length > 2000 ? value.slice(0, 2000) : value;
              onChange("summary", truncatedValue);
            }}
            className="w-full min-h-30 px-3 py-2 text-sm bg-transparent focus:outline-none transition-colors resize-none border-0 overflow-hidden"
            placeholder="Enter user summary (up to 2000 characters)"
            style={{ height: "auto" }}
            onInput={(e) => {
              e.currentTarget.style.height = "auto";
              e.currentTarget.style.height =
                e.currentTarget.scrollHeight + "px";
            }}
          />
        </ScrollArea>
      </div>

      {/* Instagram */}
      <div className="space-y-2">
        <Label htmlFor="form-instagram" className="text-sm font-medium">
          Instagram
        </Label>
        <Input
          id="form-instagram"
          type="text"
          value={data?.instagram || ""}
          onChange={(e) => onChange("instagram", e.target.value)}
          className="bg-background/50 border-border/50 focus:border-primary transition-colors"
          placeholder="Enter Instagram"
        />
      </div>

      {/* Facebook */}
      <div className="space-y-2">
        <Label htmlFor="form-facebook" className="text-sm font-medium">
          Facebook
        </Label>
        <Input
          id="form-facebook"
          type="text"
          value={data?.facebook || ""}
          onChange={(e) => onChange("facebook", e.target.value)}
          className="bg-background/50 border-border/50 focus:border-primary transition-colors"
          placeholder="Enter Facebook"
        />
      </div>

      {/* LinkedIn */}
      <div className="space-y-2">
        <Label htmlFor="form-linkedin" className="text-sm font-medium">
          LinkedIn
        </Label>
        <Input
          id="form-linkedin"
          type="text"
          value={data?.linkedin || ""}
          onChange={(e) => onChange("linkedin", e.target.value)}
          className="bg-background/50 border-border/50 focus:border-primary transition-colors"
          placeholder="Enter LinkedIn"
        />
      </div>

      {/* Status and Role */}
      <div className="flex gap-4">
        <div className="flex-1 space-y-2">
          <Label htmlFor="form-status" className="text-sm font-medium">
            Status
          </Label>
          <Select
            value={data?.status || statusSelection[0].value}
            onValueChange={(value) => onChange("status", value as EUserStatus)}
          >
            <SelectTrigger
              id="form-status"
              className="bg-background/50 border-border/50"
            >
              <SelectValue placeholder="Select status" />
            </SelectTrigger>
            <SelectContent>
              {statusSelection.map((item) => (
                <SelectItem key={item.value} value={item.value}>
                  {item.label}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>

        <div className="flex-1 space-y-2">
          <Label htmlFor="form-role" className="text-sm font-medium">
            Role
          </Label>
          <Select
            value={data?.role || roleSelection[0].value}
            onValueChange={(value) => onChange("role", value as EUserRole)}
          >
            <SelectTrigger
              id="form-role"
              className="bg-background/50 border-border/50"
            >
              <SelectValue placeholder="Select role" />
            </SelectTrigger>

            <SelectContent>
              {roleSelection.map((item) => (
                <SelectItem key={item.value} value={item.value}>
                  {item.label}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>
      </div>
    </div>
  );
};
