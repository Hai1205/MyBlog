import { UserCog } from "lucide-react";
import { AdminDialog } from "../dialog/AdminDialog";
import UserForm from "./UserForm";
import { ExtendedUserData } from "./constant";

interface CreateUserDialogProps {
  isOpen: boolean;
  onOpenChange: (open: boolean) => void;
  onChange: (field: keyof IUser, value: string | boolean) => void;
  data: IUser | null;
  onUserCreated: () => void;
  previewAvatar: string;
  handleAvatarChange: (e: React.ChangeEvent<HTMLInputElement>) => void;
}

const CreateUserDialog = ({
  isOpen,
  onOpenChange,
  onChange,
  data,
  onUserCreated,
  previewAvatar,
  handleAvatarChange,
}: CreateUserDialogProps) => {
  return (
    <AdminDialog<IUser>
      isOpen={isOpen}
      onOpenChange={onOpenChange}
      title="Tạo người dùng"
      description="Tạo thông tin người dùng"
      icon={UserCog}
      onSubmit={onUserCreated}
      isCreateDialog={true}
      className="max-w-lg"
    >
      <UserForm
        data={data as ExtendedUserData | null}
        onChange={(field, value) => onChange(field as keyof IUser, value)}
        previewAvatar={previewAvatar}
        handleAvatarChange={handleAvatarChange}
        isCreateDialog={true}
      />
    </AdminDialog>
  );
};

export default CreateUserDialog;
