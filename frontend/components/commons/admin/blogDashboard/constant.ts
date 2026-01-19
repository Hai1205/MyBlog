import { capitalizeFirstLetter } from "@/lib/utils";
import { ECategory } from "@/types/enum";

export const blogCategories = Object.values(ECategory).map(value => ({
  value,
  label: capitalizeFirstLetter(value),
}));