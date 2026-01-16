import { Input } from "@/components/ui/input";
import { Search } from "lucide-react";

interface TableSearchProps {
  handleSearch: (e: React.FormEvent) => void;
  searchQuery: string;
  setSearchQuery: (query: string) => void;
  placeholder: string;
}

export const TableSearch = ({
  handleSearch,
  searchQuery,
  setSearchQuery,
  placeholder,
}: TableSearchProps) => {
  return (
    <form onSubmit={handleSearch} className="relative group">
      <Search
        className="absolute left-3 top-1/2 transform -translate-y-1/2 text-muted-foreground group-focus-within:text-primary w-4 h-4 transition-colors z-10 pointer-events-none"
      />
      <Input
        type="text"
        placeholder={placeholder}
        value={searchQuery}
        onChange={(e) => setSearchQuery(e.target.value)}
        className="pl-10 h-9 w-[300px] border-border/50 bg-background/50 backdrop-blur-sm focus:border-primary/50 focus:ring-primary/20 transition-all"
        onKeyDown={(e) => {
          if (e.key === "Enter") {
            e.preventDefault();
            handleSearch(e);
          }
        }}
      />
    </form>
  );
};
