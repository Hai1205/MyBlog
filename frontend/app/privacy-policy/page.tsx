export default function PrivacyPolicy() {
  return (
    <div className="min-h-screen py-12 bg-linear-to-b from-background to-muted/20">
      <div className="container max-w-4xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="bg-card border border-border rounded-lg shadow-lg p-8 md:p-12">
          <h1 className="text-4xl font-bold mb-8 bg-linear-to-br from-primary to-secondary bg-clip-text text-transparent">
            Privacy Policy
          </h1>
          <div className="prose dark:prose-invert max-w-none space-y-6">
            <p>
              This Privacy Policy describes how MyBlog collects, uses, and
              protects your information.
            </p>
            <h2 className="text-2xl font-semibold mt-8 mb-4 text-primary">
              1. Information We Collect
            </h2>
            <p className="text-muted-foreground leading-relaxed">
              We collect information you provide when registering an account,
              creating blogs, and using our services.
            </p>
            <h2 className="text-2xl font-semibold mt-8 mb-4 text-primary">
              2. How We Use Your Information
            </h2>
            <p className="text-muted-foreground leading-relaxed">
              Information is used to provide services, improve user experience,
              and comply with legal requirements.
            </p>
            <h2 className="text-2xl font-semibold mt-8 mb-4 text-primary">
              3. Information Sharing
            </h2>
            <p className="text-muted-foreground leading-relaxed">
              We do not sell, trade, or rent your personal information to third
              parties, unless required by law.
            </p>
            <h2 className="text-2xl font-semibold mt-8 mb-4 text-primary">
              4. Data Security
            </h2>
            <p className="text-muted-foreground leading-relaxed">
              We implement appropriate security measures to protect your
              information from unauthorized access.
            </p>
            <h2 className="text-2xl font-semibold mt-8 mb-4 text-primary">
              5. Your Rights
            </h2>
            <p className="text-muted-foreground leading-relaxed">
              You have the right to access, update, or delete your personal
              information at any time.
            </p>
            <h2 className="text-2xl font-semibold mt-8 mb-4 text-primary">
              6. Policy Changes
            </h2>
            <p className="text-muted-foreground leading-relaxed">
              We may update this Policy. We will notify you of any significant
              changes.
            </p>
            <p className="text-muted-foreground leading-relaxed mt-8">
              If you have any questions about this Privacy Policy, please
              contact us.
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}
