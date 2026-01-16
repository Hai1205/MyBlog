import { saveAs } from "file-saver";

export const exportToPDF = async (elementId: string, filename: string = "CV.pdf"): Promise<void> => {
    try {
        const element = document.getElementById(elementId);
        if (!element) {
            throw new Error(`Element with ID "${elementId}" not found`);
        }

        const clonedElement = element.cloneNode(true) as HTMLElement;

        // Optimization: Song song hóa các tác vụ
        await Promise.all([
            applyInlineStyles(clonedElement),
            transformHTMLForPDF(clonedElement),
            convertImagesToBase64(clonedElement), // Tách riêng function
        ]);

        const wrapper = document.createElement("div");
        wrapper.appendChild(clonedElement);

        const htmlContent = generateHTMLTemplate(wrapper.innerHTML, filename);

        // Gửi request
        const response = await fetch("/api/export-cv", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify({
                html: htmlContent,
                filename: filename,
            }),
        });

        if (!response.ok) {
            const error = await response.json().catch(() => ({ error: response.statusText }));
            console.error("API Error Details:", error);
            throw new Error(error.error || error.message || `Failed to export PDF (${response.status})`);
        }

        const blob = await response.blob();
        saveAs(blob, filename);

        return Promise.resolve();
    } catch (error) {
        console.error("PDF export error:", error);
        throw error;
    }
};

// Optimization: Convert images song song
const convertImagesToBase64 = async (element: HTMLElement): Promise<void> => {
    const images = element.querySelectorAll("img");
    
    // Tạo array các promises để convert song song
    const imagePromises = Array.from(images).map(async (img) => {
        if (img.src && !img.src.startsWith("data:")) {
            try {
                const response = await fetch(img.src);
                const blob = await response.blob();
                const base64 = await blobToBase64(blob);
                img.src = base64;
            } catch (e) {
                console.warn("Could not convert image to base64:", img.src, e);
            }
        }
    });

    // Đợi tất cả images convert xong
    await Promise.all(imagePromises);
};

// Optimization: Giảm số lượng styles cần apply
const applyInlineStyles = (element: HTMLElement): void => {
    const allElements = [element, ...Array.from(element.querySelectorAll("*"))];

    // Chỉ apply cho các elements quan trọng
    const criticalStyles = [
        "background-color",
        "color",
        "border-bottom",
        "font-size",
        "font-weight",
        "line-height",
        "padding",
        "margin",
    ];

    allElements.forEach((el) => {
        if (!(el instanceof HTMLElement)) return;

        const computedStyle = window.getComputedStyle(el);
        let styleString = "";
        
        criticalStyles.forEach((prop) => {
            const value = computedStyle.getPropertyValue(prop);
            if (value && value !== "none" && value !== "normal" && value !== "0px") {
                styleString += `${prop}: ${value}; `;
            }
        });

        if (styleString) {
            el.setAttribute("style", styleString + (el.getAttribute("style") || ""));
        }
    });
};

const transformHTMLForPDF = (element: HTMLElement): void => {
    const container = element.querySelector('[id="cv-preview-content"]') as HTMLElement;
    if (container) container.className = "cv-container";

    const header = element.querySelector('.bg-gray-100') as HTMLElement;
    if (header) {
        header.className = "cv-header";
        const headerContent = header.querySelector('.flex.items-start.gap-6') as HTMLElement;
        if (headerContent) headerContent.className = "cv-header-content";
    }

    const avatarImg = element.querySelector('[class*="Avatar"] img') as HTMLElement;
    if (avatarImg) avatarImg.className = "cv-avatar";

    const avatarFallback = element.querySelector('[class*="AvatarFallback"]') as HTMLElement;
    if (avatarFallback) avatarFallback.className = "cv-avatar-fallback";

    const name = element.querySelector('h1.text-4xl') as HTMLElement;
    if (name) name.className = "cv-name";

    const title = element.querySelector('p.text-lg') as HTMLElement;
    if (title) title.className = "cv-title";

    const contactGrid = element.querySelector('.grid.grid-cols-1') as HTMLElement;
    if (contactGrid) {
        contactGrid.className = "cv-contact-grid";
        const contactItems = contactGrid.querySelectorAll('.flex.items-center.gap-2');
        contactItems.forEach((item) => {
            (item as HTMLElement).className = "cv-contact-item";
            const label = item.querySelector('.font-semibold') as HTMLElement;
            if (label) label.className = "cv-contact-label";
        });
    }

    const sections = element.querySelectorAll('.space-y-6 > div');
    sections.forEach((section) => {
        (section as HTMLElement).className = "cv-section";
        const sectionTitle = section.querySelector('h2.border-b-2') as HTMLElement;
        if (sectionTitle) sectionTitle.className = "cv-section-title";

        const content = section.querySelector('p.text-sm') as HTMLElement;
        if (content) content.className = "cv-section-content";

        const items = section.querySelectorAll('.space-y-4 > div');
        items.forEach((item) => {
            (item as HTMLElement).className = "cv-item";
            const date = item.querySelector('.text-xs') as HTMLElement;
            if (date) date.className = "cv-item-date";

            const itemTitle = item.querySelector('h3.font-bold') as HTMLElement;
            if (itemTitle) itemTitle.className = "cv-item-title";

            const subtitle = item.querySelector('p.font-semibold') as HTMLElement;
            if (subtitle) subtitle.className = "cv-item-subtitle";

            const description = item.querySelector('p.leading-relaxed') as HTMLElement;
            if (description) description.className = "cv-item-description";
        });

        const skillsList = section.querySelector('ul.list-disc') as HTMLElement;
        if (skillsList) skillsList.className = "cv-skills-list";
    });
};

// Tách riêng HTML template generation
const generateHTMLTemplate = (content: string, filename: string): string => {
    return `<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>${filename}</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; -webkit-print-color-adjust: exact !important; print-color-adjust: exact !important; }
        body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif; background: white; }
        .cv-container { max-width: 210mm; margin: 0 auto; background: white; padding: 2rem; min-height: 297mm; }
        .cv-header { margin: -2rem -2rem 2rem -2rem; background-color: #f3f4f6 !important; padding: 1.5rem 2rem; border-bottom: 2px solid #e5e7eb !important; }
        .cv-header-content { display: flex; align-items: flex-start; gap: 1.5rem; }
        .cv-avatar { width: 6rem; height: 6rem; border-radius: 50%; border: 4px solid white; box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1); object-fit: cover; }
        .cv-avatar-fallback { width: 6rem; height: 6rem; border-radius: 50%; border: 4px solid white; box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1); background-color: #1e3a8a; color: white; display: flex; align-items: center; justify-content: center; font-size: 1.5rem; font-weight: 700; }
        .cv-name { font-size: 2.25rem; font-weight: 700; color: #1e3a8a; margin-bottom: 0.5rem; }
        .cv-title { font-size: 1.125rem; font-weight: 500; color: #4b5563; margin-bottom: 1rem; }
        .cv-contact-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 0.5rem; font-size: 0.875rem; color: #374151; }
        .cv-section { margin-top: 1.5rem; }
        .cv-section-title { font-size: 1.25rem; font-weight: 700; color: #1e3a8a; padding-bottom: 0.25rem; margin-bottom: 0.75rem; border-bottom: 2px solid #1e3a8a !important; }
        .cv-section-content { font-size: 0.875rem; line-height: 1.625; color: #374151; text-align: justify; }
        .cv-item { margin-top: 1rem; }
        .cv-item-date { font-size: 0.75rem; color: #6b7280; font-weight: 500; margin-bottom: 0.25rem; }
        .cv-item-title { font-size: 1rem; font-weight: 700; color: #111827; margin-bottom: 0.25rem; }
        .cv-item-subtitle { font-size: 0.875rem; font-weight: 600; color: #374151; margin-bottom: 0.5rem; }
        .cv-item-description { font-size: 0.875rem; line-height: 1.625; color: #374151; text-align: justify; }
        .cv-skills-list { list-style-type: disc; margin-left: 1.25rem; font-size: 0.875rem; color: #374151; }
    </style>
</head>
<body>${content}</body>
</html>`;
};

export const exportCustomHTML = async (html: string, filename: string = "document.pdf"): Promise<void> => {
    try {
        const response = await fetch("/api/export-cv", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify({
                html: html,
                filename: filename,
            }),
        });

        if (!response.ok) {
            const error = await response.json().catch(() => ({ error: response.statusText }));
            throw new Error(error.error || error.message || `Failed to export PDF (${response.status})`);
        }

        const blob = await response.blob();
        saveAs(blob, filename);
    } catch (error) {
        console.error("PDF export error (Custom HTML):", error);
        throw error;
    }
};

const blobToBase64 = (blob: Blob): Promise<string> => {
    return new Promise((resolve, reject) => {
        const reader = new FileReader();
        reader.onloadend = () => resolve(reader.result as string);
        reader.onerror = reject;
        reader.readAsDataURL(blob);
    });
};