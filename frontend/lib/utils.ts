import { clsx, type ClassValue } from "clsx"
import { twMerge } from "tailwind-merge"
import { formatDistanceToNow } from 'date-fns/formatDistanceToNow'
import { parseISO } from 'date-fns/parseISO'

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs))
}

export const TOTAL_MS_IN_DAY = 24 * 60 * 60 * 1000;

export const formatDateAgo = (date: string, style?: string) => {
  const createdDate = date ? parseISO(date) : new Date();
  const now = new Date();

  const diffInMs = now.getTime() - createdDate.getTime();
  const diffInDays = diffInMs / (TOTAL_MS_IN_DAY);

  if (diffInDays < 7) {
    return formatDistanceToNow(createdDate, { addSuffix: true });
  } else {
    switch (style) {
      case "DDMMYYY":
        return formatDateInDDMMYYY(date);
      case "YYYYMMDD":
        return formatDateInYYYYMMDD(date);
      default:
        return formatDateInDDMMYYY(date);
    }
  }
};

export const formatTime = (seconds: number) => {
  const minutes = Math.floor(seconds / 60);
  const remainingSeconds = Math.floor(seconds % 60);

  return `${minutes}:${remainingSeconds.toString().padStart(2, "0")}`;
};

export const formatDateInDDMMYYY = (date: string) => {
  const d = new Date(date);
  const day = String(d.getUTCDate()).padStart(2, '0');
  const month = String(d.getUTCMonth() + 1).padStart(2, '0');
  const year = d.getUTCFullYear();

  return `${day}/${month}/${year}`;
}

export const formatDateInYYYYMMDD = (date: string) => {
  const dateObj = new Date(date);
  const year = dateObj.getFullYear();
  const month = String(dateObj.getMonth() + 1).padStart(2, '0');
  const day = String(dateObj.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
}

export const formatNumberStyle = (value: number): string => {
  if (value < 1_000) {
    return value.toString();
  } else if (value < 1_000_000) {
    return (value / 1_000).toFixed(value >= 10_000 ? 0 : 1).replace('.', ',') + 'K';
  } else if (value < 1_000_000_000) {
    return (value / 1_000_000).toFixed(value >= 10_000_000 ? 0 : 1).replace('.', ',') + 'M';
  } else {
    return (value / 1_000_000_000).toFixed(value >= 10_000_000_000 ? 0 : 1).replace('.', ',') + 'B';
  }
}

export const validatePhoneNumber = (phone: string): boolean => {
  // Vietnamese phone number regex
  // Format: 0[35789]xxxxxxxx (10 digits total)
  // Starts with 0, followed by 3,5,7,8,9, then 8 more digits
  const phoneRegex = /^0[35789][0-9]{8}$/;
  const cleanedPhone = phone.replace(/\s+/g, "");
  return phoneRegex.test(cleanedPhone);
}

export const formatCurrency = (amount: number): string => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
  }).format(amount);
}

export const formatSlug = (text: string): string => {
  return text
    .toLowerCase()
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .replace(/[^a-z0-9\s-]/g, '')
    .replace(/\s+/g, '-')
    .trim();
}

export const truncateText = (text: string, length: number): string => {
  if (text.length <= length) return text;
  return text.substring(0, length) + '...';
}

export const capitalizeEachWord = (input: string): string => {
  if (!input) return '';

  return input
    .toLowerCase()
    .split(' ')
    .map(word =>
      word.charAt(0).toUpperCase() + word.slice(1)
    )
    .join(' ');
}

export const testFormData = (formData: FormData) => {
  formData.forEach((value, key) => {
    console.log(`${key}: ${value}`);
  });
}

export const stringToList = (str: string): string[] => {
  return str
    .split(",")
    .map(item => item.trim())
    .filter(Boolean);
}

export const cleanString = (input: string): string => {
  return input
    .split(",")
    .map(item => item.trim())
    .filter(Boolean)
    .join(",");
}

export const capitalizeFirstLetter = (value: string | number): string => {
  const str = value.toString();
  return str.charAt(0).toUpperCase() + str.slice(1);
}