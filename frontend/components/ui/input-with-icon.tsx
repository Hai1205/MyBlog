import * as React from "react";
import { LucideIcon } from "lucide-react";
import { Input } from "@/components/ui/input";
import { cn } from "@/lib/utils";

export interface InputWithIconProps
  extends React.InputHTMLAttributes<HTMLInputElement> {
  leftIcon?: LucideIcon;
  rightIcon?: LucideIcon;
  onRightIconClick?: () => void;
  rightIconClassName?: string;
}

const InputWithIcon = React.forwardRef<HTMLInputElement, InputWithIconProps>(
  (
    {
      className,
      leftIcon: LeftIcon,
      rightIcon: RightIcon,
      onRightIconClick,
      rightIconClassName,
      ...props
    },
    ref
  ) => {
    return (
      <div className="relative">
        {LeftIcon && (
          <LeftIcon className="absolute left-3 top-2.5 h-4 w-4 text-muted-foreground" />
        )}
        <Input
          ref={ref}
          className={cn(LeftIcon && "pl-10", RightIcon && "pr-10", className)}
          {...props}
        />
        {RightIcon && (
          <button
            type="button"
            onClick={onRightIconClick}
            className={cn(
              "absolute right-3 top-2.5 text-muted-foreground hover:text-foreground",
              rightIconClassName
            )}
          >
            <RightIcon className="h-4 w-4" />
          </button>
        )}
      </div>
    );
  }
);

InputWithIcon.displayName = "InputWithIcon";

export { InputWithIcon };
