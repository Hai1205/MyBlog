"use client";

import { SharedFilter } from "../adminTable/SharedFilter";
import { IUserFilter, UserFilterType, roleSelection, statusSelection } from "./UserDashboardClient";

interface UserFilterProps {
  openMenuFilters: boolean;
  setOpenMenuFilters: (open: boolean) => void;
  activeFilters: IUserFilter;
  toggleFilter: (value: string, type: UserFilterType) => void;
  clearFilters: () => void;
  applyFilters: () => void;
  closeMenuFilters: () => void;
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
  closeMenuFilters,
}: UserFilterProps) => {
  const filterSections: UserFilterSection[] = [
    {
      key: "status",
      label: "Status",
      options: statusSelection,
    },
    {
      key: "role",
      label: "Role",
      options: roleSelection,
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
      closeMenuFilters={closeMenuFilters}
      filterSections={filterSections}
    />
  );
};
