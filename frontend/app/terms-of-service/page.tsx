export default function TermsOfService() {
  return (
    <div className="min-h-screen py-12 bg-linear-to-b from-background to-muted/20">
      <div className="container max-w-4xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="bg-card border border-border rounded-lg shadow-lg p-8 md:p-12">
          <h1 className="text-4xl font-bold mb-8 bg-linear-to-br from-primary to-secondary bg-clip-text text-transparent">
            Terms of Service
          </h1>
          <div className="prose dark:prose-invert max-w-none space-y-6">
            <p>Welcome to MyBlog!</p>
            <p>
              These terms and conditions ("Terms") govern your use of our
              service. By accessing or using MyBlog, you agree to comply with
              these Terms.
            </p>
            <h2 className="text-2xl font-semibold mt-8 mb-4 text-primary">
              1. Acceptance of Terms
            </h2>
            <p className="text-muted-foreground leading-relaxed">
              By using our service, you acknowledge that you have read,
              understood, and agree to all the terms and conditions set forth in
              this document.
            </p>
            <h2 className="text-2xl font-semibold mt-8 mb-4 text-primary">
              2. Service Description
            </h2>
            <p className="text-muted-foreground leading-relaxed">
              MyBlog is an online platform that helps users create and manage
              professional blogs with AI-powered assistance.
            </p>
            <h2 className="text-2xl font-semibold mt-8 mb-4 text-primary">
              3. User Rights and Obligations
            </h2>
            <p className="text-muted-foreground leading-relaxed">
              You are responsible for providing accurate and up-to-date
              information. You may not use the service for any illegal purposes.
            </p>
            <h2 className="text-2xl font-semibold mt-8 mb-4 text-primary">
              4. Intellectual Property Rights
            </h2>
            <p className="text-muted-foreground leading-relaxed">
              All intellectual property rights related to MyBlog belong to us or
              our licensors.
            </p>
            <h2 className="text-2xl font-semibold mt-8 mb-4 text-primary">
              5. Termination
            </h2>
            <p className="text-muted-foreground leading-relaxed">
              We reserve the right to terminate or suspend your account at any
              time if you violate these Terms.
            </p>
            <h2 className="text-2xl font-semibold mt-8 mb-4 text-primary">
              6. Changes to Terms
            </h2>
            <p className="text-muted-foreground leading-relaxed">
              We may update these Terms at any time. Continued use of the
              service after changes constitutes acceptance of the new terms.
            </p>
            <p className="text-muted-foreground leading-relaxed mt-8">
              If you have any questions, please contact us.
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}
