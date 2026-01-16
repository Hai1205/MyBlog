interface DraggingOnPageProps {
  title: string;
  subtitle: string;
}

export default function DraggingOnPage({
  title,
  subtitle,
}: DraggingOnPageProps) {
  return (
    <div className="fixed inset-0 bg-primary/10 backdrop-blur-sm z-50 flex items-center justify-center pointer-events-none">
      <div className="bg-background border-2 border-dashed border-primary rounded-lg p-12 shadow-2xl">
        <div className="flex flex-col items-center gap-4">
          <div className="w-20 h-20 rounded-full bg-primary/20 flex items-center justify-center">
            <svg
              className="w-10 h-10 text-primary"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"
              />
            </svg>
          </div>
          <div className="text-center">
            <p className="text-2xl font-bold text-primary mb-2">
              {title}
            </p>
            <p className="text-sm text-muted-foreground">
              {subtitle}
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}
