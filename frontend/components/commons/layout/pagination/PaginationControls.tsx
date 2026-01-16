import {
  Pagination,
  PaginationContent,
  PaginationEllipsis,
  PaginationItem,
  PaginationLink,
  PaginationNext,
  PaginationPrevious,
} from "@/components/ui/pagination";
import { cn } from "@/lib/utils";

export interface PaginationData {
  currentPage: number;
  totalPages: number;
  totalElements: number;
  pageSize: number;
  hasNext: boolean;
  hasPrevious: boolean;
}

interface PaginationControlsProps {
  paginationData: PaginationData;
  onPageChange: (page: number) => void;
  className?: string;
  showFirstLast?: boolean;
}

export function PaginationControls({
  paginationData,
  onPageChange,
  className,
  showFirstLast = true,
}: PaginationControlsProps) {
  const {
    currentPage,
    totalPages,
    totalElements,
    pageSize,
    hasNext,
    hasPrevious,
  } = paginationData;

  // Generate page numbers to display
  const getPageNumbers = () => {
    const pages: (number | "ellipsis")[] = [];
    const maxVisiblePages = 5;

    if (totalPages <= maxVisiblePages + 2) {
      // Show all pages if total is small
      for (let i = 1; i <= totalPages; i++) {
        pages.push(i);
      }
    } else {
      // Always show first page
      pages.push(1);

      if (currentPage <= 3) {
        // Near the beginning
        for (let i = 2; i <= Math.min(maxVisiblePages, totalPages - 1); i++) {
          pages.push(i);
        }
        pages.push("ellipsis");
      } else if (currentPage >= totalPages - 2) {
        // Near the end
        pages.push("ellipsis");
        for (let i = totalPages - (maxVisiblePages - 1); i < totalPages; i++) {
          pages.push(i);
        }
      } else {
        // In the middle
        pages.push("ellipsis");
        for (let i = currentPage - 1; i <= currentPage + 1; i++) {
          pages.push(i);
        }
        pages.push("ellipsis");
      }

      // Always show last page
      pages.push(totalPages);
    }

    return pages;
  };

  const pageNumbers = getPageNumbers();

  // Calculate showing range
  const startItem = (currentPage - 1) * pageSize + 1;
  const endItem = Math.min(currentPage * pageSize, totalElements);

  if (totalPages <= 1) return null;

  return (
    <div className={cn("flex flex-col items-center gap-4 mt-6", className)}>
      {/* Info text */}
      <div className="text-sm text-muted-foreground bg-card/50 backdrop-blur-sm px-4 py-2 rounded-lg border border-border/50">
        Showing{" "}
        <span className="font-semibold text-foreground">{startItem}</span> to{" "}
        <span className="font-semibold text-foreground">{endItem}</span> of{" "}
        <span className="font-semibold text-foreground">{totalElements}</span>{" "}
        results
      </div>

      {/* Pagination controls */}
      <Pagination>
        <PaginationContent className="bg-card/80 backdrop-blur-sm px-2 py-2 rounded-xl border border-border/50 shadow-lg">
          {/* First page button */}
          {showFirstLast && currentPage > 1 && (
            <PaginationItem>
              <PaginationLink
                onClick={() => onPageChange(1)}
                className="rounded-lg hover:scale-105 transition-transform"
              >
                <span className="text-xs">First</span>
              </PaginationLink>
            </PaginationItem>
          )}

          {/* Previous button */}
          {hasPrevious && (
            <PaginationItem>
              <PaginationPrevious
                onClick={() => onPageChange(currentPage - 1)}
                className="rounded-lg hover:scale-105 transition-transform"
              />
            </PaginationItem>
          )}

          {/* Page numbers */}
          {pageNumbers.map((page, index) =>
            page === "ellipsis" ? (
              <PaginationItem key={`ellipsis-${index}`}>
                <PaginationEllipsis />
              </PaginationItem>
            ) : (
              <PaginationItem key={page}>
                <PaginationLink
                  onClick={() => onPageChange(page)}
                  isActive={currentPage === page}
                  className={cn(
                    "rounded-lg hover:scale-105 transition-transform min-w-10",
                    currentPage === page && "pointer-events-none"
                  )}
                >
                  {page}
                </PaginationLink>
              </PaginationItem>
            )
          )}

          {/* Next button */}
          {hasNext && (
            <PaginationItem>
              <PaginationNext
                onClick={() => onPageChange(currentPage + 1)}
                className="rounded-lg hover:scale-105 transition-transform"
              />
            </PaginationItem>
          )}

          {/* Last page button */}
          {showFirstLast && currentPage < totalPages && (
            <PaginationItem>
              <PaginationLink
                onClick={() => onPageChange(totalPages)}
                className="rounded-lg hover:scale-105 transition-transform"
              >
                <span className="text-xs">Last</span>
              </PaginationLink>
            </PaginationItem>
          )}
        </PaginationContent>
      </Pagination>
    </div>
  );
}
