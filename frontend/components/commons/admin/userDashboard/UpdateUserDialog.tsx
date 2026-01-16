import { UserCog } from "lucide-react";
import { AdminDialog } from "../dialog/AdminDialog";
import UserForm from "./UserForm";
import { ExtendedUserData } from "./constant";

interface UpdateUserDialogProps {
  isOpen: boolean;
  onOpenChange: (open: boolean) => void;
  onChange: (field: keyof IUser, value: string | boolean) => void;
  data: IUser | null;
  onUserUpdated: () => void;
  previewAvatar: string;
  handleAvatarChange: (e: React.ChangeEvent<HTMLInputElement>) => void;
}

const UpdateUserDialog = ({
  isOpen,
  onOpenChange,
  onChange,
  data,
  onUserUpdated,
  previewAvatar,
  handleAvatarChange,
}: UpdateUserDialogProps) => {
  return (
    <AdminDialog<IUser>
      isOpen={isOpen}
      onOpenChange={onOpenChange}
      title="Chỉnh sửa người dùng"
      description="Cập nhật thông tin quản trị viên"
      icon={UserCog}
      onSubmit={onUserUpdated}
      isCreateDialog={false}
      className="max-w-lg"
    >
      <UserForm
        data={data as ExtendedUserData | null}
        onChange={(field, value) => onChange(field as keyof IUser, value)}
        previewAvatar={previewAvatar}
        handleAvatarChange={handleAvatarChange}
        isCreateDialog={false}
      />
    </AdminDialog>
  );
};

export default UpdateUserDialog;
