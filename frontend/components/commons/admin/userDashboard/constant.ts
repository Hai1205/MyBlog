import { capitalizeFirstLetter } from "@/lib/utils";
import { EUserRole, EUserStatus } from "@/types/enum";

export const userStatus = Object.values(EUserStatus).map(value => ({
  value,
  label: capitalizeFirstLetter(value),
}));

export const userRole = Object.values(EUserRole).map(value => ({
  value,
  label: capitalizeFirstLetter(value),
}));

export type ExtendedUserData = Omit<IUser, "status"> & {
  status: EUserStatus;
  role: EUserRole;
  planExpiration?: string;
  password?: string;
};