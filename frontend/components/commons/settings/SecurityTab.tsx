"use client";

import { Button } from "@/components/ui/button";
import { InputWithIcon } from "@/components/ui/input-with-icon";
import { Eye, EyeOff, Lock, Loader2, Trash2 } from "lucide-react";
import { FormEvent, useState } from "react";
import { ConfirmationDialog } from "@/components/commons/layout/ConfirmationDialog";
import { toast } from "react-toastify";

type ExtendedUserData = Omit<IUser, "status"> & {
  newPassword?: string;
  currentPassword?: string;
  confirmPassword?: string;
};

interface SecurityTabProps {
  data: ExtendedUserData | null;
  onChange: (
    field: keyof ExtendedUserData,
    value: string | string[] | boolean,
  ) => void;
  onChangePassword: (e: FormEvent) => void;
  isLoading?: boolean;
}

export const SecurityTab = ({
  data,
  onChange,
  onChangePassword,
  isLoading = false,
}: SecurityTabProps) => {
  const [showCurrentPassword, setShowCurrentPassword] = useState(false);
  const [showNewPassword, setShowNewPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [showDeleteDialog, setShowDeleteDialog] = useState(false);

  const handleDeleteAccount = () => {
    // Implement actual delete logic here
    toast.success("Account deletion request has been sent!");
    setShowDeleteDialog(false);
  };

  return (
    <>
      <form onSubmit={onChangePassword} className="space-y-6">
        <InputWithIcon
          id="current-password"
          name="current-password"
          type={showCurrentPassword ? "text" : "password"}
          placeholder="Enter your current password"
          value={data?.currentPassword || ""}
          onChange={(e) => onChange("currentPassword", e.target.value)}
          leftIcon={Lock}
          rightIcon={showCurrentPassword ? EyeOff : Eye}
          onRightIconClick={() => setShowCurrentPassword(!showCurrentPassword)}
        />

        <InputWithIcon
          id="new-password"
          name="new-password"
          type={showNewPassword ? "text" : "password"}
          placeholder="Enter your new password"
          value={data?.newPassword || ""}
          onChange={(e) => onChange("newPassword", e.target.value)}
          leftIcon={Lock}
          rightIcon={showNewPassword ? EyeOff : Eye}
          onRightIconClick={() => setShowNewPassword(!showNewPassword)}
        />

        <InputWithIcon
          id="confirm-password"
          name="confirm-password"
          type={showConfirmPassword ? "text" : "password"}
          placeholder="Re-enter new password"
          value={data?.confirmPassword || ""}
          onChange={(e) => onChange("confirmPassword", e.target.value)}
          leftIcon={Lock}
          rightIcon={showConfirmPassword ? EyeOff : Eye}
          onRightIconClick={() => setShowConfirmPassword(!showConfirmPassword)}
        />

        <div className="flex justify-end pt-4">
          <Button
            type="submit"
            disabled={isLoading}
            className="bg-linear-to-br from-primary to-secondary hover:from-primary/90 hover:to-secondary/90 shadow-lg shadow-primary/30 hover:shadow-xl hover:shadow-primary/40 transition-all duration-200 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {isLoading ? (
              <>
                <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                Processing...
              </>
            ) : (
              "Change Password"
            )}
          </Button>
        </div>
      </form>

      {/* Danger Zone */}
      <div className="mt-8 rounded-lg border border-destructive/50 bg-destructive/5 p-6">
        <div className="space-y-4">
          <div>
            <h3 className="text-lg font-semibold text-destructive">
              Danger Zone
            </h3>
            <p className="mt-1 text-sm text-muted-foreground">
              This action cannot be undone. Please consider carefully before
              proceeding.
            </p>
          </div>
          <Button
            variant="destructive"
            onClick={() => setShowDeleteDialog(true)}
            className="w-full sm:w-auto"
          >
            <Trash2 className="mr-2 h-4 w-4" />
            Delete account permanently
          </Button>
        </div>
      </div>

      <ConfirmationDialog
        open={showDeleteDialog}
        onOpenChange={setShowDeleteDialog}
        onConfirm={handleDeleteAccount}
        title="Delete account permanently?"
        description="All your data including blogs, personal information and payment history will be permanently deleted. This action cannot be undone!"
        confirmText="Delete account"
        cancelText="Cancel"
        isDestructive={true}
      />
    </>
  );
};
