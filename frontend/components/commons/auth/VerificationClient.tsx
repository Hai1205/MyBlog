"use client";

import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Alert, AlertDescription } from "@/components/ui/alert";
import type React from "react";
import {
  ClipboardEvent,
  FormEvent,
  KeyboardEvent,
  useEffect,
  useRef,
  useState,
} from "react";
import { useRouter } from "next/navigation";
import { toast } from "react-toastify";
import {
  useVerifyOTPMutation,
  useSendOTPMutation,
} from "@/hooks/api/mutations/useAuthMutations";
import Link from "next/link";
import { Loader2, ArrowLeft, Shield, Clock } from "lucide-react";

interface VerificationClientProps {
  identifier: string | null;
  isActivation: boolean;
}

const VerificationClient = ({
  identifier,
  isActivation,
}: VerificationClientProps) => {
  const { mutateAsync: verifyOTPMutateAsync, isPending: isVerifying } =
    useVerifyOTPMutation();
  const { mutateAsync: sendOTPMutateAsync, isPending: isSendingOTP } =
    useSendOTPMutation();

  const router = useRouter();
  const [otp, setOtp] = useState<string[]>(Array(6).fill(""));

  const [isExpired, setIsExpired] = useState(false);
  const [timeLeft, setTimeLeft] = useState(300);
  const inputRefs = useRef<(HTMLInputElement | null)[]>([]);

  const isLoading = isVerifying || isSendingOTP;

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

  const handlePaste = (e: ClipboardEvent<HTMLInputElement>) => {
    e.preventDefault();
    const pastedData = e.clipboardData.getData("text/plain").trim();

    if (/^\d{6}$/.test(pastedData)) {
      const newOtp = pastedData.split("");
      setOtp(newOtp);
      inputRefs.current[5]?.focus();
    }
  };

  const handleKeyDown = (index: number, e: KeyboardEvent<HTMLInputElement>) => {
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

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();

    if (!validate() || !identifier) {
      return;
    }

    try {
      await verifyOTPMutateAsync(
        {
          identifier,
          data: {
            otp: otp.join(""),
            isActivation,
          },
        },
        {
          onSuccess: () => {
            if (!isActivation) {
              router.push(
                `/auth/reset-password/?identifier=${encodeURIComponent(identifier)}`,
              );
            } else {
              router.push("/auth/login");
            }
          },
        },
      );
    } catch (error) {
      console.error("OTP verification failed:", error);
    } finally {
      setOtp(Array(6).fill(""));
    }
  };

  const handleResend = async () => {
    if (!identifier) return;

    try {
      const result = await sendOTPMutateAsync(identifier);

      if (result) {
        setOtp(Array(6).fill(""));
        setTimeLeft(300);
        setIsExpired(false);
      }
    } catch (error) {
      console.error("Failed to resend OTP:", error);
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
        <h1 className="text-2xl font-bold tracking-tight">
          Enter verification code
        </h1>
        <p className="text-muted-foreground">
          We have sent a 6-digit OTP code to your account{" "}
          <strong>{identifier}</strong>
        </p>
        <p className="text-sm text-muted-foreground">
          Please enter the code to{" "}
          {isActivation ? "verify your account" : "reset your password"}
        </p>
      </div>

      {!isExpired && (
        <div className="flex items-center justify-center gap-2 p-3 bg-primary/5 rounded-lg border">
          <Clock className="h-4 w-4 text-primary" />
          <span className="text-sm font-medium">
            Code expires in: {formatTime(timeLeft)}
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
              Verifying...
            </>
          ) : (
            "Verify"
          )}
        </Button>
      </form>

      <div className="space-y-4 text-center">
        <p className="text-sm text-muted-foreground">
          Didn't receive the code?{" "}
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
