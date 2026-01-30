"use client";

import { useRouter } from "next/navigation";
import { ChangeEvent, FormEvent, useEffect, useState } from "react";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { User, Lock } from "lucide-react";
import { useAuthStore } from "@/stores/authStore";
import { toast } from "react-toastify";
import { EUserRole, EUserStatus } from "@/types/enum";
import { ProfileTab } from "@/components/commons/settings/ProfileTab";
import { SecurityTab } from "@/components/commons/settings/SecurityTab";
import { useUpdateUserMutation } from "@/hooks/api/mutations/useUserMutations";
import { useChangePasswordMutation } from "@/hooks/api/mutations/useAuthMutations";

export default function SettingsClient() {
  const { userAuth } = useAuthStore();

  const { mutateAsync: updateUserAsync } = useUpdateUserMutation();
  const { mutateAsync: changePasswordAsync, isPending: isChangingPassword } =
    useChangePasswordMutation();

  const router = useRouter();
  const [avatarFile, setAvatarFile] = useState<File | null>(null);
  const [previewAvatar, setPreviewAvatar] = useState<string>("");

  type ExtendedUserData = Omit<IUser, "status"> & {
    status: EUserStatus;
    role: EUserRole;
    password?: string;
    newPassword?: string;
    currentPassword?: string;
    confirmPassword?: string;
  };

  const defaultUser: ExtendedUserData = {
    id: "",
    username: "",
    email: "",
    password: "",
    newPassword: "",
    confirmPassword: "",
    instagram: "",
    facebook: "",
    linkedin: "",
    role: EUserRole.USER,
    status: EUserStatus.PENDING,
    createdAt: "",
    updatedAt: "",
  };

  const [data, setData] = useState<ExtendedUserData | null>(userAuth);

  const handleChange = (
    field: keyof ExtendedUserData,
    value: string | string[] | boolean,
  ) => {
    setData((prev) =>
      prev ? { ...prev, [field]: value } : { ...defaultUser, [field]: value },
    );
  };

  const handleUpdate = async () => {
    if (data) {
      updateUserAsync({
        userId: data.id,
        data: {
          birth: data.birth || "",
          summary: data.summary || "",
          avatar: avatarFile,
          role: data.role,
          status: data.status,
          instagram: data.instagram || "",
          facebook: data.facebook || "",
          linkedin: data.linkedin || "",
        },
      });
    }
  };

  const handleAvatarChange = (e: ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      setAvatarFile(file);
      setPreviewAvatar(URL.createObjectURL(file));
    }
  };

  const handleChangePassword = async (e: FormEvent) => {
    e.preventDefault();

    if (data?.newPassword !== data?.confirmPassword) {
      toast.error("New passwords do not match!");
      return;
    }
    if (data?.newPassword && data.newPassword.length < 6) {
      toast.error("Password must be at least 6 characters!");
      return;
    }

    if (
      !data?.email ||
      !data?.currentPassword ||
      !data?.newPassword ||
      !data?.confirmPassword
    ) {
      toast.error("Please fill in all password fields!");
      return;
    }

    changePasswordAsync(
      {
        identifier: data.email,
        data: {
          currentPassword: data.currentPassword,
          newPassword: data.newPassword,
          confirmPassword: data.confirmPassword,
        },
      },
      {
        onSuccess: () => {
          setData((prev) =>
            prev
              ? {
                  ...prev,
                  currentPassword: "",
                  newPassword: "",
                  confirmPassword: "",
                }
              : null,
          );
        },
      },
    );
  };

  useEffect(() => {
    if (!userAuth) {
      router.push("/auth/login");
    }
  }, [userAuth]);

  if (!userAuth) return null;

  const tabContents = [
    {
      value: "profile",
      icon: User,
      title: "Profile",
      description: "Update your profile information",
      component: (
        <ProfileTab
          data={data}
          onChange={handleChange}
          onUpdate={handleUpdate}
          previewAvatar={previewAvatar}
          onAvatarChange={handleAvatarChange}
        />
      ),
    },
    {
      value: "security",
      icon: Lock,
      title: "Security",
      description: "Change password and security settings",
      component: (
        <SecurityTab
          data={data}
          onChange={handleChange}
          onChangePassword={handleChangePassword}
          isLoading={isChangingPassword}
        />
      ),
    },
  ];

  return (
    <div className="min-h-screen flex items-center justify-center py-12 bg-linear-to-br from-background to-muted/20">
      <div className="container max-w-4xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="mb-8">
          <h1 className="text-3xl font-bold bg-linear-to-br from-primary to-secondary bg-clip-text text-transparent">
            Settings
          </h1>
          <p className="text-muted-foreground mt-2">
            Manage your account information and settings
          </p>
        </div>

        <Tabs defaultValue="profile" className="space-y-6">
          <TabsList className="grid w-full grid-cols-2 lg:grid-cols-4 bg-card border border-border/50">
            {tabContents.map(({ value, icon: Icon, title }) => (
              <TabsTrigger
                key={value}
                value={value}
                className="gap-2 data-[state=active]:bg-linear-to-br data-[state=active]:from-primary data-[state=active]:to-secondary data-[state=active]:text-primary-foreground"
              >
                <Icon className="h-4 w-4" />
                <span className="hidden sm:inline">{title}</span>
              </TabsTrigger>
            ))}
          </TabsList>

          {tabContents.map(({ value, title, description, component }) => (
            <TabsContent key={value} value={value}>
              <Card className="border-border/50 shadow-lg bg-linear-to-br from-card to-card/80 backdrop-blur-sm">
                <CardHeader>
                  <CardTitle className="text-xl bg-linear-to-br from-primary to-secondary bg-clip-text text-transparent">
                    {title}
                  </CardTitle>
                  <CardDescription>{description}</CardDescription>
                </CardHeader>
                <CardContent>{component}</CardContent>
              </Card>
            </TabsContent>
          ))}
        </Tabs>
      </div>
    </div>
  );
}
