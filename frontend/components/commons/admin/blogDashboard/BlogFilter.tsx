"use client";

import { SharedFilter } from "../adminTable/SharedFilter";
import { capitalizeFirstLetter } from "@/lib/utils";
import { IBlogFilter, BlogFilterType } from "./BlogDashboardClient";
import { ECategory } from "@/types/enum";

interface BlogFilterProps {
  openMenuFilters: boolean;
  setOpenMenuFilters: (open: boolean) => void;
  activeFilters: IBlogFilter;
  toggleFilter: (value: string, type: BlogFilterType) => void;
  clearFilters: () => void;
  applyFilters: () => void;
  closeMenuMenuFilters: () => void;
}

interface BlogFilterSection {
  key: BlogFilterType;
  label: string;
  options: { label: string; value: string }[];
}

export const BlogFilter = ({
  openMenuFilters,
  setOpenMenuFilters,
  activeFilters,
  toggleFilter,
  clearFilters,
  applyFilters,
  closeMenuMenuFilters,
}: BlogFilterProps) => {
  const filterSections: BlogFilterSection[] = [
    {
      key: "category",
      label: "Category",
      options: Object.values(ECategory).map((status) => ({
        label: capitalizeFirstLetter(status),
        value: status,
      })),
    },
  ];

  return (
    <SharedFilter<BlogFilterType, BlogFilterSection>
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
