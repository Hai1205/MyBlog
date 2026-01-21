"use client";

import { EUserRole, EUserStatus } from "@/types/enum";
import { SharedFilter } from "../adminTable/SharedFilter";
import { capitalizeFirstLetter } from "@/lib/utils";
import { IUserFilter, UserFilterType } from "./UserDashboardClient";
import { userRole, userStatus } from "./constant";

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
      label: "Status",
      options: userStatus,
    },
    {
      key: "role",
      label: "Role",
      options: userRole,
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
