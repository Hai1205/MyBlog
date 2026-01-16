"use client";

import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Alert, AlertDescription } from "@/components/ui/alert";
import type React from "react";
import { useEffect, useRef, useState } from "react";
import { useRouter } from "next/navigation";
import { toast } from "react-toastify";
import { useAuthStore } from "@/stores/authStore";
import Link from "next/link";
import { Loader2, ArrowLeft, Shield, Clock } from "lucide-react";

interface VerificationClientProps {
  identifier: string | null;
  isActivation: boolean;
}

const VerificationClient: React.FC<VerificationClientProps> = ({
  identifier: initialIdentifier,
  isActivation: initialIsActivation,
}) => {
  const { isLoading, verifyOTP, sendOTP } = useAuthStore();
  const router = useRouter();
  const [identifier, setIdentifier] = useState(initialIdentifier || "");
  const [isActivation, setIsActivation] = useState(initialIsActivation);
  const [otp, setOtp] = useState<string[]>(Array(6).fill(""));

  const [isExpired, setIsExpired] = useState(false);
  const [timeLeft, setTimeLeft] = useState(300);
  const inputRefs = useRef<(HTMLInputElement | null)[]>([]);

  useEffect(() => {
    if (timeLeft <= 0) {
      setIsExpired(true);
      return;
    }

    const timer = setInterval(() => {
      setTimeLeft((prevTime) => prevTime - 1);
    }, 1000);

    return () => clearInterval(timer);
  }, [timeLeft]);

  const handleChange = (index: number, value: string) => {
    if (!/^\d*$/.test(value)) return;

    const newOtp = [...otp];
    newOtp[index] = value.substring(0, 1);
    setOtp(newOtp);

    if (value && index < 5) {
      inputRefs.current[index + 1]?.focus();
    }
  };

  const handlePaste = (e: React.ClipboardEvent<HTMLInputElement>) => {
    e.preventDefault();
    const pastedData = e.clipboardData.getData("text/plain").trim();

    if (/^\d{6}$/.test(pastedData)) {
      const newOtp = pastedData.split("");
      setOtp(newOtp);
      inputRefs.current[5]?.focus();
    }
  };

  const handleKeyDown = (
    index: number,
    e: React.KeyboardEvent<HTMLInputElement>
  ) => {
    if (e.key === "Backspace") {
      if (otp[index] === "" && index > 0) {
        const newOtp = [...otp];
        newOtp[index - 1] = "";
        setOtp(newOtp);
        inputRefs.current[index - 1]?.focus();
        return;
      }

      const newOtp = [...otp];
      newOtp[index] = "";
      setOtp(newOtp);
    } else if (e.key === "ArrowLeft") {
      e.preventDefault();
      if (index > 0) {
        inputRefs.current[index - 1]?.focus();
      } else {
        inputRefs.current[5]?.focus();
      }
    } else if (e.key === "ArrowRight") {
      e.preventDefault();
      if (index < 5) {
        inputRefs.current[index + 1]?.focus();
      } else {
        inputRefs.current[0]?.focus();
      }
    }
  };

  const validate = () => {
    if (otp.some((digit) => digit === "")) {
      return false;
    }
    return true;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!validate()) {
      return;
    }

    const res = await verifyOTP(identifier, otp.join(""), isActivation);

    if (!res) {
      setOtp(Array(6).fill(""));
      return;
    }

    if (isExpired) {
      setOtp(Array(6).fill(""));
      return;
    }

    if (res?.status && res.status !== 200) {
      return;
    }

    if (!isActivation) {
      router.push(
        `/auth/reset-password/?identifier=${encodeURIComponent(identifier)}`
      );
    } else {
      toast.success("Account verified successfully");
      router.push("/auth/login");
    }
  };

  const handleResend = async () => {
    const result = await sendOTP(identifier);

    if (result) {
      toast.success("OTP code has been resent");
      setOtp(Array(6).fill(""));
      setTimeLeft(300);
      setIsExpired(false);
    }
  };

  const formatTime = (seconds: number) => {
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${mins}:${secs < 10 ? "0" : ""}${secs}`;
  };

  return (
    <div className="space-y-6">
      <div className="space-y-2 text-center">
        <div className="flex justify-center mb-4">
          <div className="h-12 w-12 rounded-full bg-primary/10 flex items-center justify-center">
            <Shield className="h-6 w-6 text-primary" />
          </div>
        </div>
        <h1 className="text-2xl font-bold tracking-tight">Nhập mã xác thực</h1>
        <p className="text-muted-foreground">
          Chúng tôi đã gửi mã OTP gồm 6 chữ số về tài khoản{" "}
          <strong>{identifier}</strong>
        </p>
        <p className="text-sm text-muted-foreground">
          Vui lòng nhập mã để{" "}
          {isActivation ? "xác thực tài khoản" : "đặt lại mật khẩu"}
        </p>
      </div>

      {!isExpired && (
        <div className="flex items-center justify-center gap-2 p-3 bg-primary/5 rounded-lg border">
          <Clock className="h-4 w-4 text-primary" />
          <span className="text-sm font-medium">
            Mã hết hạn trong: {formatTime(timeLeft)}
          </span>
        </div>
      )}

      {isExpired && (
        <Alert variant="destructive">
          <AlertDescription>
            OTP code has expired. Please request a new code.
          </AlertDescription>
        </Alert>
      )}

      <form onSubmit={handleSubmit} className="space-y-6">
        <div className="flex justify-center gap-2">
          {[0, 1, 2, 3, 4, 5].map((index) => (
            <Input
              key={index}
              ref={(el) => {
                inputRefs.current[index] = el;
              }}
              type="text"
              value={otp[index]}
              onChange={(e) => handleChange(index, e.target.value)}
              onKeyDown={(e) => handleKeyDown(index, e)}
              onPaste={index === 0 ? handlePaste : undefined}
              className={`w-12 h-12 text-center text-xl font-bold ${
                isExpired ? "opacity-50" : ""
              }`}
              maxLength={1}
              disabled={isExpired}
            />
          ))}
        </div>

        <Button
          type="submit"
          className="w-full"
          disabled={isLoading || isExpired || !validate()}
        >
          {isLoading ? (
            <>
              <Loader2 className="mr-2 h-4 w-4 animate-spin" />
              Đang xác thực...
            </>
          ) : (
            "Xác thực"
          )}
        </Button>
      </form>

      <div className="space-y-4 text-center">
        <p className="text-sm text-muted-foreground">
          Không nhận được mã?{" "}
          <button
            onClick={handleResend}
            className="text-primary hover:underline font-medium"
            disabled={isLoading}
          >
            Resend Code
          </button>
        </p>

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

export default VerificationClient;
