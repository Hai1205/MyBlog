import ResetPasswordClient from "@/components/commons/auth/ResetPasswordClient";

interface PageProps {
  searchParams: { identifier?: string };
}

export default function ResetPasswordPage({ searchParams }: PageProps) {
  return <ResetPasswordClient identifier={searchParams.identifier || null} />;
}
