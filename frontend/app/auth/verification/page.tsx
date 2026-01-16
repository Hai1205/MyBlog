import VerificationClient from "@/components/commons/auth/VerificationClient";

interface PageProps {
  searchParams: { identifier?: string; isActivation?: string };
}

export default function VerificationPage({ searchParams }: PageProps) {
  const identifier = searchParams.identifier || null;
  const isActivation = searchParams.isActivation === "true";
  
  return <VerificationClient identifier={identifier} isActivation={isActivation} />;
}
