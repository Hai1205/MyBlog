import {
  Pagination,
  PaginationContent,
  PaginationEllipsis,
  PaginationItem,
  PaginationLink,
  PaginationNext,
  PaginationPrevious,
} from "@/components/ui/pagination";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { cn } from "@/lib/utils";

export interface PaginationData {
  currentPage: number;
  totalPages: number;
  totalElements: number;
  pageSize: number;
  hasNext: boolean;
  hasPrevious: boolean;
}

interface GridPaginationProps {
  paginationData: PaginationData;
  onPageChange: (page: number) => void;
  onPageSizeChange?: (size: number) => void;
  className?: string;
  showFirstLast?: boolean;
  showPageSizeSelector?: boolean;
  pageSizeOptions?: number[];
}

export function GridPagination({
  paginationData,
  onPageChange,
  onPageSizeChange,
  className,
  showFirstLast = true,
  showPageSizeSelector = true,
  pageSizeOptions = [8, 12, 16, 24, 32],
}: GridPaginationProps) {
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

  if (totalPages <= 1 && !showPageSizeSelector) return null;

  return (
    <div className={cn("flex flex-col items-center gap-6 mt-8", className)}>
      {/* Pagination controls */}
      {totalPages > 1 && (
        <Pagination>
          <PaginationContent className="bg-card/80 backdrop-blur-md px-3 py-3 rounded-2xl border border-border/50 shadow-xl shadow-primary/5">
            {/* First page button */}
            {showFirstLast && currentPage > 1 && (
              <PaginationItem>
                <PaginationLink
                  onClick={() => onPageChange(1)}
                  className="rounded-xl hover:scale-105 hover:shadow-md hover:shadow-primary/20 transition-all duration-200"
                >
                  <span className="text-xs font-semibold">First</span>
                </PaginationLink>
              </PaginationItem>
            )}

            {/* Previous button */}
            {hasPrevious && (
              <PaginationItem>
                <PaginationPrevious
                  onClick={() => onPageChange(currentPage - 1)}
                  className="rounded-xl hover:scale-105 hover:shadow-md hover:shadow-primary/20 transition-all duration-200"
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
                      "rounded-xl hover:scale-105 hover:shadow-md hover:shadow-primary/20 transition-all duration-200 min-w-12 font-semibold",
                      currentPage === page &&
                        "pointer-events-none bg-linear-to-br from-primary to-secondary text-primary-foreground shadow-lg shadow-primary/30",
                    )}
                  >
                    {page}
                  </PaginationLink>
                </PaginationItem>
              ),
            )}

            {/* Next button */}
            {hasNext && (
              <PaginationItem>
                <PaginationNext
                  onClick={() => onPageChange(currentPage + 1)}
                  className="rounded-xl hover:scale-105 hover:shadow-md hover:shadow-primary/20 transition-all duration-200"
                />
              </PaginationItem>
            )}

            {/* Last page button */}
            {showFirstLast && currentPage < totalPages && (
              <PaginationItem>
                <PaginationLink
                  onClick={() => onPageChange(totalPages)}
                  className="rounded-xl hover:scale-105 hover:shadow-md hover:shadow-primary/20 transition-all duration-200"
                >
                  <span className="text-xs font-semibold">Last</span>
                </PaginationLink>
              </PaginationItem>
            )}
          </PaginationContent>
        </Pagination>
      )}

      {/* Info and page size selector row */}
      <div className="flex flex-col sm:flex-row items-center justify-between gap-4 w-full max-w-4xl">
        {/* Info text */}
        <div className="text-sm text-muted-foreground bg-card/60 backdrop-blur-sm px-5 py-2.5 rounded-xl border border-border/50 shadow-md">
          Showing{" "}
          <span className="font-bold text-foreground bg-linear-to-br from-primary/20 to-secondary/20 px-2 py-0.5 rounded">
            {startItem}-{endItem}
          </span>{" "}
          of{" "}
          <span className="font-bold text-foreground bg-linear-to-br from-primary/20 to-secondary/20 px-2 py-0.5 rounded">
            {totalElements}
          </span>{" "}
          results
        </div>

        {/* Page size selector */}
        {showPageSizeSelector && onPageSizeChange && (
          <div className="flex items-center gap-3 bg-card/60 backdrop-blur-sm px-5 py-2.5 rounded-xl border border-border/50 shadow-md">
            <span className="text-sm text-muted-foreground font-medium">
              Per page:
            </span>
            <Select
              value={pageSize.toString()}
              onValueChange={(value) => onPageSizeChange(parseInt(value))}
            >
              <SelectTrigger className="w-20 h-9 bg-background/80 backdrop-blur-sm border-border/50 rounded-lg hover:border-primary/50 focus:ring-primary/30 transition-all shadow-sm">
                <SelectValue />
              </SelectTrigger>
              <SelectContent className="bg-card/95 backdrop-blur-md border-border/50 rounded-xl shadow-xl">
                {pageSizeOptions.map((size) => (
                  <SelectItem
                    key={size}
                    value={size.toString()}
                    className="rounded-lg hover:bg-linear-to-br hover:from-primary/10 hover:to-secondary/10 cursor-pointer font-medium"
                  >
                    {size}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
        )}
      </div>
    </div>
  );
}
