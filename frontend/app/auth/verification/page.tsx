import VerificationClient from "@/components/commons/auth/VerificationClient";

interface PageProps {
  searchParams: Promise<{ identifier?: string; isActivation?: string }>;
}

export default async function VerificationPage({ searchParams }: PageProps) {
  const params = await searchParams;
  const identifier = params.identifier || null;
  const isActivation = params.isActivation === "true";

  return (
    <VerificationClient identifier={identifier} isActivation={isActivation} />
  );
}
