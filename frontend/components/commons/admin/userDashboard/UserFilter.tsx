"use client";

import { EUserRole, EUserStatus } from "@/types/enum";
import { SharedFilter } from "../adminTable/SharedFilter";
import { capitalizeFirstLetter } from "@/lib/utils";
import { IUserFilter, UserFilterType } from "./UserDashboardClient";

interface UserFilterProps {
  openMenuFilters: boolean;
  setOpenMenuFilters: (open: boolean) => void;
  activeFilters: IUserFilter;
  toggleFilter: (value: string, type: UserFilterType) => void;
  clearFilters: () => void;
  applyFilters: () => void;
  closeMenuMenuFilters: () => void;
}

interface UserFilterSection {
  key: UserFilterType;
  label: string;
  options: { label: string; value: string }[];
}

export const UserFilter = ({
  openMenuFilters,
  setOpenMenuFilters,
  activeFilters,
  toggleFilter,
  clearFilters,
  applyFilters,
  closeMenuMenuFilters,
}: UserFilterProps) => {
  const filterSections: UserFilterSection[] = [
    {
      key: "status",
      label: "Trạng thái",
      options: Object.values(EUserStatus).map((status) => ({
        label: capitalizeFirstLetter(status),
        value: status,
      })),
    },
    {
      key: "role",
      label: "Vai trò",
      options: Object.values(EUserRole).map((role) => ({
        label: capitalizeFirstLetter(role),
        value: role,
      })),
    },
  ];

  return (
    <SharedFilter<UserFilterType, UserFilterSection>
      openMenuFilters={openMenuFilters}
      setOpenMenuFilters={setOpenMenuFilters}
      activeFilters={activeFilters as Record<string, string[]>}
      toggleFilter={toggleFilter}
      clearFilters={clearFilters}
      applyFilters={applyFilters}
      closeMenuMenuFilters={closeMenuMenuFilters}
      filterSections={filterSections}
    />
  );
};
