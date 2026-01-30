"use client";

import { useState, useEffect } from "react";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { ScrollArea } from "@/components/ui/scroll-area";
import { Download, FileText, Loader2 } from "lucide-react";
import { toast } from "react-toastify";
import { useStatsReportBlob } from "@/hooks/api/queries/useStatsQueries";

interface ReportViewerDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
}

export function ReportViewerDialog({
  open,
  onOpenChange,
}: ReportViewerDialogProps) {
  const {
    blob: statsReport,
    isLoading: isLoadingReport,
    error: reportError,
  } = useStatsReportBlob({ enabled: open });

  const [pdfUrl, setPdfUrl] = useState<string | null>(null);
  const [loadError, setLoadError] = useState(false);

  // Cleanup effect for pdfUrl
  useEffect(() => {
    return () => {
      if (pdfUrl) {
        URL.revokeObjectURL(pdfUrl);
      }
    };
  }, [pdfUrl]);

  // Load report effect
  useEffect(() => {
    if (!open || !statsReport) {
      setPdfUrl(null);
      return;
    }

    try {
      setLoadError(false);
      const url = URL.createObjectURL(statsReport);
      setPdfUrl(url);
    } catch (error) {
      console.error("Error creating PDF URL:", error);
      setLoadError(true);
      toast.error("Failed to load report");
    }
  }, [open, statsReport]);

  // Handle error state
  useEffect(() => {
    if (reportError && open) {
      setLoadError(true);
      toast.error("Failed to load report");
    }
  }, [reportError, open]);

  const handleDownload = () => {
    if (!statsReport) {
      toast.error("No report available to download");
      return;
    }

    const url = URL.createObjectURL(statsReport);
    const link = document.createElement("a");
    link.href = url;
    link.download = `dashboard-report-${new Date().getTime()}.pdf`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(url);

    toast.success("Report downloaded successfully");
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-[98vw] w-full h-[95vh] flex flex-col">
        <DialogHeader>
          <DialogTitle className="flex items-center gap-2">
            <FileText className="h-5 w-5" />
            Dashboard Statistics Report
          </DialogTitle>
          <DialogDescription>
            View and download your dashboard statistics report
          </DialogDescription>
        </DialogHeader>

        <div className="flex-1 flex flex-col gap-4 overflow-hidden">
          {isLoadingReport || (!pdfUrl && !loadError) ? (
            <div className="flex-1 flex items-center justify-center">
              <div className="flex flex-col items-center gap-3">
                <Loader2 className="h-8 w-8 animate-spin text-primary" />
                <p className="text-sm text-muted-foreground">
                  Generating report...
                </p>
              </div>
            </div>
          ) : pdfUrl ? (
            <>
              <ScrollArea className="flex-1 border rounded-lg bg-muted">
                <div className="w-full min-h-[70vh]">
                  <iframe
                    src={pdfUrl}
                    className="w-full h-[80vh]"
                    title="Dashboard Report"
                  />
                </div>
              </ScrollArea>

              <div className="flex justify-end gap-2">
                <Button variant="outline" onClick={() => onOpenChange(false)}>
                  Close
                </Button>
                <Button onClick={handleDownload} className="gap-2">
                  <Download className="h-4 w-4" />
                  Download Report
                </Button>
              </div>
            </>
          ) : loadError ? (
            <div className="flex-1 flex items-center justify-center">
              <div className="text-center">
                <FileText className="h-12 w-12 mx-auto text-muted-foreground mb-3" />
                <p className="text-muted-foreground">
                  Failed to load report. Please try again.
                </p>
                <Button
                  onClick={() => window.location.reload()}
                  variant="outline"
                  className="mt-4"
                >
                  Retry
                </Button>
              </div>
            </div>
          ) : null}
        </div>
      </DialogContent>
    </Dialog>
  );
}
