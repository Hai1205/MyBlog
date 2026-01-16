"use client";

import { Button } from "@/components/ui/button";
import { InputWithIcon } from "@/components/ui/input-with-icon";
import { Label } from "@/components/ui/label";
import { Alert, AlertDescription } from "@/components/ui/alert";
import type React from "react";
import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { toast } from "react-toastify";
import { useAuthStore } from "@/stores/authStore";
import Link from "next/link";
import { Loader2, Lock, Eye, EyeOff, ArrowLeft, KeyRound } from "lucide-react";

interface ResetPasswordClientProps {
  identifier: string | null;
}

const ResetPasswordClient: React.FC<ResetPasswordClientProps> = ({
  identifier: initialIdentifier,
}) => {
  const { forgotPassword, isLoading } = useAuthStore();
  const router = useRouter();
  const [identifier, setIdentifier] = useState(initialIdentifier || "");
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);

  const [formData, setFormData] = useState({
    newPassword: "",
    rePassword: "",
  });
  const [errors, setErrors] = useState<Record<string, string>>({});

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
    if (errors[name]) {
      setErrors((prev) => ({ ...prev, [name]: "" }));
    }
  };

  const validate = () => {
    const newErrors: Record<string, string> = {};
    if (!formData.newPassword) {
      newErrors.newPassword = "Please enter new password";
    } else if (formData.newPassword.length < 8) {
      newErrors.newPassword = "New password must be at least 8 characters";
    }

    if (!formData.rePassword) {
      newErrors.rePassword = "Please confirm your password";
    } else if (formData.newPassword !== formData.rePassword) {
      newErrors.rePassword = "Passwords do not match";
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!validate()) {
      return;
    }

    const res = await forgotPassword(
      identifier,
      formData.newPassword,
      formData.rePassword
    );

    if (!res) {
      return;
    }

    toast.success("Password changed successfully");
    router.push("/auth/login");
  };

  return (
    <div className="space-y-6">
      <div className="space-y-2 text-center">
        <div className="flex justify-center mb-4">
          <div className="h-12 w-12 rounded-full bg-primary/10 flex items-center justify-center">
            <KeyRound className="h-6 w-6 text-primary" />
          </div>
        </div>
        <h1 className="text-2xl font-bold tracking-tight">Reset Password</h1>
        <p className="text-muted-foreground">
          Create a new password for account <strong>{identifier}</strong>
        </p>
      </div>

      <form onSubmit={handleSubmit} className="space-y-4">
        <div className="space-y-2">
          <Label htmlFor="newPassword">Mật khẩu mới</Label>
          <InputWithIcon
            id="newPassword"
            name="newPassword"
            type={showPassword ? "text" : "password"}
            placeholder="Nhập mật khẩu mới"
            value={formData.newPassword}
            onChange={handleChange}
            leftIcon={Lock}
            rightIcon={showPassword ? EyeOff : Eye}
            onRightIconClick={() => setShowPassword(!showPassword)}
          />
          {errors.newPassword && (
            <Alert variant="destructive">
              <AlertDescription>{errors.newPassword}</AlertDescription>
            </Alert>
          )}
          <p className="text-xs text-muted-foreground">
            Mật khẩu phải có ít nhất 8 ký tự
          </p>
        </div>

        <div className="space-y-2">
          <Label htmlFor="rePassword">Xác nhận mật khẩu</Label>
          <InputWithIcon
            id="rePassword"
            name="rePassword"
            type={showConfirmPassword ? "text" : "password"}
            placeholder="Nhập lại mật khẩu mới"
            value={formData.rePassword}
            onChange={handleChange}
            leftIcon={Lock}
            rightIcon={showConfirmPassword ? EyeOff : Eye}
            onRightIconClick={() =>
              setShowConfirmPassword(!showConfirmPassword)
            }
          />
          {errors.rePassword && (
            <Alert variant="destructive">
              <AlertDescription>{errors.rePassword}</AlertDescription>
            </Alert>
          )}
        </div>

        <Button type="submit" className="w-full" disabled={isLoading}>
          {isLoading ? (
            <>
              <Loader2 className="mr-2 h-4 w-4 animate-spin" />
              Đang đặt lại...
            </>
          ) : (
            "Reset Password"
          )}
        </Button>
      </form>

      <div className="text-center">
        <Link
          href="/auth/login"
          className="inline-flex items-center gap-2 text-sm text-primary hover:underline"
        >
          <ArrowLeft className="h-3 w-3" />
          Back to login
        </Link>
      </div>
    </div>
  );
};

export default ResetPasswordClient;
