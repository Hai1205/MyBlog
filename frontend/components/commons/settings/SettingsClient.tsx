"use client";

import { useRouter } from "next/navigation";
import { useEffect, useState } from "react";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { User, Lock, Receipt, CreditCard } from "lucide-react";
import { useAuthStore } from "@/stores/authStore";
import { useUserStore } from "@/stores/userStore";
import { toast } from "react-toastify";
import { EUserRole, EUserStatus } from "@/types/enum";
import ProfileTab from "@/components/commons/settings/ProfileTab";
import SecurityTab from "@/components/commons/settings/SecurityTab";

export default function SettingsClient() {
  const { userAuth, changePassword } = useAuthStore();
  const { updateUser } = useUserStore();

  const router = useRouter();
  const [avatarFile, setAvatarFile] = useState<File | null>(null);
  const [previewAvatar, setPreviewAvatar] = useState<string>("");
  const [isChangingPassword, setIsChangingPassword] = useState(false);

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
    fullname: "",
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
    value: string | string[] | boolean
  ) => {
    setData((prev) =>
      prev ? { ...prev, [field]: value } : { ...defaultUser, [field]: value }
    );
  };

  const handleUpdate = async () => {
    if (data) {
      await updateUser(
        data.id,
        data.fullname,
        data.birth || "",
        data.summary || "",
        avatarFile || null,
        data.role,
        data.status,
        data.instagram || "",
        data.facebook || "",
        data.linkedin || ""
      );
    }
  };

  const handleAvatarChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      setAvatarFile(file);
      setPreviewAvatar(URL.createObjectURL(file));
    }
  };

  const handleChangePassword = async (e: React.FormEvent) => {
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
      !data?.email &&
      !data?.currentPassword &&
      !data?.newPassword &&
      !data?.confirmPassword
    ) {
      toast.error("Please fill in all password fields!");
      return;
    }

    setIsChangingPassword(true);

    const res = await changePassword(
      data.email,
      data.currentPassword || "",
      data.newPassword || "",
      data.confirmPassword || ""
    );

    if (res.data?.success) {
      toast.success("Password changed successfully!");
      setData((prev) =>
        prev
          ? {
              ...prev,
              currentPassword: "",
              newPassword: "",
              confirmPassword: "",
            }
          : null
      );
    }

    setIsChangingPassword(false);
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
      title: "Thông Tin Cá Nhân",
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
      title: "Bảo Mật",
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
    // {
    //   value: "billing",
    //   icon: Receipt,
    //   title: "Thanh Toán",
    //   description: "Xem lịch sử thanh toán và hóa đơn",
    //   component: null,
    // },
    // {
    //   value: "plan",
    //   icon: CreditCard,
    //   title: "Quản Lý Plan",
    //   description: "Xem và nâng cấp plan của bạn",
    //   component: null,
    // },
  ];

  return (
    <div className="min-h-screen flex items-center justify-center py-12 bg-linear-to-br from-background to-muted/20">
      <div className="container max-w-4xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="mb-8">
          <h1 className="text-3xl font-bold bg-linear-to-br from-primary to-secondary bg-clip-text text-transparent">
            Cài Đặt
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
