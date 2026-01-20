import { Button } from "@/components/ui/button";
import { Checkbox } from "@/components/ui/checkbox";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { ScrollArea } from "@/components/ui/scroll-area";
import { Filter } from "lucide-react";

interface SharedFilterProps<T = string, S = any> {
  openMenuFilters: boolean;
  setOpenMenuFilters: (open: boolean) => void;
  activeFilters: Record<string, string[]>;
  toggleFilter: (value: string, type: T) => void;
  clearFilters: () => void;
  applyFilters: () => void;
  closeMenuMenuFilters: () => void;
  filterSections: S[];
}

export const SharedFilter = <
  T = string,
  S extends {
    key: string;
    label: string;
    options: { label: string; value: string }[];
  } = any,
>({
  openMenuFilters,
  setOpenMenuFilters,
  activeFilters,
  toggleFilter,
  clearFilters,
  applyFilters,
  closeMenuMenuFilters,
  filterSections,
}: SharedFilterProps<T, S>) => {
  return (
    <DropdownMenu open={openMenuFilters} onOpenChange={closeMenuMenuFilters}>
      <DropdownMenuTrigger asChild>
        <Button
          variant="secondary"
          size="sm"
          className="h-9 gap-2 px-4 bg-linear-to-br from-secondary/80 to-secondary hover:from-secondary hover:to-secondary/90 shadow-md hover:shadow-lg hover:shadow-secondary/20 transition-all duration-200 hover:scale-105"
          onClick={() => setOpenMenuFilters(!openMenuFilters)}
        >
          <Filter className="h-4 w-4" />
          Filter
        </Button>
      </DropdownMenuTrigger>

      <DropdownMenuContent
        align="end"
        className="w-62.5 bg-card/95 backdrop-blur-sm border border-border/50 shadow-xl"
      >
        <DropdownMenuLabel className="text-foreground font-semibold bg-linear-to-br from-primary/10 to-secondary/10">
          Filter by
        </DropdownMenuLabel>

        <DropdownMenuSeparator className="bg-border/50" />
        <ScrollArea className="max-h-100">
          {" "}
          {filterSections.map((section, index) => (
            <div key={section.key} className="p-3">
              <h4 className="mb-3 text-sm font-semibold text-foreground">
                {section.label}
              </h4>

              <div className="space-y-3">
                {section.options.map((option) => (
                  <div
                    key={option.value}
                    className="flex items-center hover:bg-primary/5 p-1.5 rounded-lg transition-colors"
                  >
                    <Checkbox
                      id={`${section.key}-${option.value}`}
                      checked={
                        activeFilters[section.key]?.includes(option.value) ||
                        false
                      }
                      onCheckedChange={() =>
                        toggleFilter(option.value, section.key as T)
                      }
                      className="mr-2 border-primary/50"
                    />

                    <label
                      htmlFor={`${section.key}-${option.value}`}
                      className="text-foreground text-sm cursor-pointer flex-1"
                    >
                      {option.label}
                    </label>
                  </div>
                ))}
              </div>

              {index < filterSections.length - 1 && (
                <DropdownMenuSeparator className="bg-border/50 mt-3" />
              )}
            </div>
          ))}
        </ScrollArea>

        <DropdownMenuSeparator className="bg-border/50" />

        <div className="p-3 flex justify-between gap-2">
          <Button
            variant="outline"
            size="sm"
            onClick={clearFilters}
            className="flex-1 border-border/50 hover:bg-muted/50 transition-all"
          >
            Clear Filters
          </Button>

          <Button
            size="sm"
            onClick={applyFilters}
            className="flex-1 bg-linear-to-br from-primary to-secondary hover:from-primary/90 hover:to-secondary/90 shadow-md hover:shadow-lg transition-all"
          >
            Apply
          </Button>
        </div>
      </DropdownMenuContent>
    </DropdownMenu>
  );
};
