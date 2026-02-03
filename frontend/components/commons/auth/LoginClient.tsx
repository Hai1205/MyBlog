"use client";

import { Button } from "@/components/ui/button";
import { InputWithIcon } from "@/components/ui/input-with-icon";
import { Label } from "@/components/ui/label";
import { Alert, AlertDescription } from "@/components/ui/alert";
import { useState, useEffect, ChangeEvent, FormEvent } from "react";
import { useRouter } from "next/navigation";
import {
  useLoginMutation,
  useSendOTPMutation,
} from "@/hooks/api/mutations/useAuthMutations";
import Link from "next/link";
import { Loader2, Mail, Lock, EyeOff, Eye } from "lucide-react";
import { useAuthStore } from "@/stores/authStore";

const LoginClient = () => {
  const { handleSetUserAuth } = useAuthStore();

  const { mutateAsync: loginMutateAsync, isPending: isLogging } =
    useLoginMutation();
  const { mutateAsync: sendOTPMutateAsync, isPending: isSendingOTP } =
    useSendOTPMutation();

  const router = useRouter();

  const [showPassword, setShowPassword] = useState(false);
  const [formData, setFormData] = useState({
    identifier: "",
    password: "",
  });
  const [errors, setErrors] = useState<Record<string, string>>({});

  const isLoading = isLogging || isSendingOTP;

  const handleChange = (e: ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));

    if (errors[name]) {
      setErrors((prev) => ({ ...prev, [name]: "" }));
    }
  };

  const validate = () => {
    const newErrors: Record<string, string> = {};

    if (!formData.identifier.trim()) {
      newErrors.identifier = "Please enter email or username";
    }

    if (!formData.password) {
      newErrors.password = "Please enter password";
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    if (!validate()) return;

    try {
      const response = await loginMutateAsync(
        {
          identifier: formData.identifier,
          data: {
            password: formData.password,
          },
        },
        {
          onSuccess: (response) => {
            const user = response?.data?.user;
            if (user) {
              handleSetUserAuth(user);
            }

            router.push(`/`);
          },
        },
      );

      const status = response?.status;
      if (!status) return;
      if (status === 403) {
        router.push(
          `/auth/verification?identifier=${encodeURIComponent(
            formData.identifier,
          )}&isActivation=true`,
        );

        await sendOTPMutateAsync(formData.identifier);
      } else if (status === 451) {
        router.push(`/auth/banned`);
      }
    } catch (error) {
      console.error("Login failed:", error);
    }
  };

  return (
    <div className="space-y-6">
      <div className="space-y-2 text-center">
        <h1 className="text-2xl font-bold tracking-tight">Login</h1>
        <p className="text-muted-foreground">
          Enter your email or username to access your account
        </p>
      </div>

      <form onSubmit={handleSubmit} className="space-y-4">
        <div className="space-y-2">
          <Label htmlFor="identifier">Email or username</Label>
          <InputWithIcon
            id="identifier"
            name="identifier"
            type="text"
            placeholder="Enter email or username"
            value={formData.identifier}
            onChange={handleChange}
            leftIcon={Mail}
          />
          {errors.identifier && (
            <Alert variant="destructive">
              <AlertDescription>{errors.identifier}</AlertDescription>
            </Alert>
          )}
        </div>

        <div className="space-y-2">
          <Label htmlFor="password">Password</Label>
          <InputWithIcon
            id="password"
            name="password"
            type={showPassword ? "text" : "password"}
            placeholder="Enter your password"
            value={formData.password}
            onChange={handleChange}
            leftIcon={Lock}
            rightIcon={showPassword ? EyeOff : Eye}
            onRightIconClick={() => setShowPassword(!showPassword)}
          />
          {errors.password && (
            <Alert variant="destructive">
              <AlertDescription>{errors.password}</AlertDescription>
            </Alert>
          )}
        </div>

        <div className="flex items-center justify-end">
          <Link
            href="/auth/forgot-password"
            className="text-sm text-primary hover:underline"
          >
            Forgot password?
          </Link>
        </div>

        <Button type="submit" className="w-full" disabled={isLoading}>
          {isLoading ? (
            <>
              <Loader2 className="mr-2 h-4 w-4 animate-spin" />
              Logging in...
            </>
          ) : (
            "Login"
          )}
        </Button>
      </form>

      <div className="text-center text-sm text-muted-foreground">
        Don't have an account?{" "}
        <Link
          href="/auth/register"
          className="text-primary hover:underline font-medium"
        >
          Sign up now
        </Link>
      </div>
    </div>
  );
};

export default LoginClient;
