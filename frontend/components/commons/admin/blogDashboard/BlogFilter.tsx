"use client";

import { SharedFilter } from "../adminTable/SharedFilter";
import { IBlogFilter, BlogFilterType, categorySelection } from "./BlogDashboardClient";

interface BlogFilterProps {
  openMenuFilters: boolean;
  setOpenMenuFilters: (open: boolean) => void;
  activeFilters: IBlogFilter;
  toggleFilter: (value: string, type: BlogFilterType) => void;
  clearFilters: () => void;
  applyFilters: () => void;
  closeMenuFilters: () => void;
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
  closeMenuFilters,
}: BlogFilterProps) => {
  const filterSections: BlogFilterSection[] = [
    {
      key: "category",
      label: "Category",
      options: categorySelection,
    },
    {
      key: "visibility",
      label: "Visibility mode",
      options: [
        { label: "Public", value: "true" },
        { label: "Private", value: "false" },
      ],
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
      closeMenuFilters={closeMenuFilters}
      filterSections={filterSections}
    />
  );
};
