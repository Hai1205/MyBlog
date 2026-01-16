"use client";

import { Button } from "@/components/ui/button";
import { InputWithIcon } from "@/components/ui/input-with-icon";
import { Label } from "@/components/ui/label";
import { Alert, AlertDescription } from "@/components/ui/alert";
import type React from "react";
import { useState } from "react";
import { useRouter } from "next/navigation";
import { toast } from "react-toastify";
import { useAuthStore } from "@/stores/authStore";
import Link from "next/link";
import { Loader2, Mail, ArrowLeft, Send } from "lucide-react";

const ForgotPasswordClient: React.FC = () => {
  const router = useRouter();
  const { isLoading, sendOTP } = useAuthStore();
  const [identifier, setIdentifier] = useState("");
  const [error, setError] = useState("");

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setIdentifier(e.target.value);
    if (error) setError("");
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    const res = await sendOTP(identifier);

    if (!res) {
      return;
    }

    toast.success("OTP code has been sent to your email");

    router.push(
      `/auth/verification?identifier=${encodeURIComponent(
        identifier
      )}&isActivation=false`
    );
  };

  return (
    <div className="space-y-6">
      <div className="space-y-2 text-center">
        <h1 className="text-2xl font-bold tracking-tight">Forgot Password</h1>
        <p className="text-muted-foreground">
          Enter your email and we'll send you an OTP code to help you reset your
          password
        </p>
      </div>

      <form onSubmit={handleSubmit} className="space-y-4">
        <div className="space-y-2">
          <Label htmlFor="identifier">Email or username</Label>
          <InputWithIcon
            id="identifier"
            type="text"
            name="identifier"
            placeholder="Enter your email or username"
            value={identifier}
            onChange={handleChange}
            leftIcon={Mail}
          />
          {error && (
            <Alert variant="destructive">
              <AlertDescription>{error}</AlertDescription>
            </Alert>
          )}
        </div>

        <Button type="submit" className="w-full" disabled={isLoading}>
          {isLoading ? (
            <>
              <Loader2 className="mr-2 h-4 w-4 animate-spin" />
              Sending...
            </>
          ) : (
            <>
              <Send className="mr-2 h-4 w-4" />
              Send OTP Code
            </>
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

export default ForgotPasswordClient;
