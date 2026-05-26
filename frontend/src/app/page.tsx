import Link from "next/link";
import Image from "next/image";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Separator } from "@/components/ui/separator";
import {
  CheckCircle2,
  Mail,
  Inbox,
  FileText,
  LayoutDashboard,
  Bell,
  FolderOpen,
  Lightbulb,
  Check,
  ArrowRight,
} from "lucide-react";

export default function Page() {
  return (
    <div className="min-h-screen font-sans">

      {/* ── HERO ── */}
      <header className="relative overflow-hidden bg-primary text-primary-foreground">
        {/* subtle grid background */}
        <div
          className="absolute inset-0 opacity-10"
          style={{
            backgroundImage:
              "linear-gradient(rgba(255,255,255,.15) 1px, transparent 1px), linear-gradient(90deg, rgba(255,255,255,.15) 1px, transparent 1px)",
            backgroundSize: "40px 40px",
          }}
        />
        <div className="relative mx-auto max-w-4xl px-6 py-28 text-center">
          <Badge
            variant="secondary"
            className="mb-6 bg-chart-1 text-primary border-0"
          >
            100 % gratuit — aucune carte requise
          </Badge>
          <h1 className="mb-6 text-4xl font-extrabold leading-tight tracking-tight sm:text-5xl lg:text-6xl">
            Reprenez le contrôle de votre{" "}
            <span className="text-chart-2">recherche d'emploi</span>
          </h1>
          <p className="mx-auto mb-10 max-w-2xl text-lg text-chart-1 sm:text-xl">
            Centralisez vos candidatures, suivez vos relances et sachez
            exactement où vous en êtes <span className="text-chart-0"> sans tableurs, sans oublis. </span>
          </p>
          <div className="flex flex-col items-center gap-4 sm:flex-row sm:justify-center">
            <Button
              //asChild
              size="lg"
              className="bg-primary-foreground hover:bg-chart-0 text-primary font-semibold px-8"
            >
              <Link href="/inscription">
                Commencer gratuitement 
              </Link>
            </Button>
            <Button
              variant="outline"
              size="lg"
              className="border-primary-foreground bg-primary text-primary-foreground hover:bg-primary-foreground hover:text-primary"
            >
              Voir comment ça fonctionne
            </Button>
          </div>
        </div>
      </header>

      {/* ── PROBLEMS ── */}
      <section className="bg-chart-0 py-20">
        <div className="mx-auto max-w-4xl px-6">
          <h2 className="mb-14 text-center text-3xl font-bold tracking-tight text-primary sm:text-4xl">
            Postuler ne suffit plus.{" "}
            <span className=" text-chart-2">Suivre fait la différence.</span>
          </h2>
          <div className="grid gap-6 sm:grid-cols-2">
            {[
              {
                icon: <CheckCircle2 className="h-6 w-6 text-primary shrink-0" />,
                text: "Vous postulez à plusieurs offres et ne savez plus lesquelles ont été relancées.",
              },
              {
                icon: <Mail className="h-6 w-6 text-primary shrink-0" />,
                text: "Vous oubliez quand recontacter une entreprise.",
              },
              {
                icon: <Inbox className="h-6 w-6 text-primary shrink-0" />,
                text: "Vos CV et lettres sont dispersés dans des dossiers ou des mails.",
              },
              {
                icon: <FileText className="h-6 w-6 text-primary shrink-0" />,
                text: "Vous perdez du temps à tout refaire à la main.",
              },
            ].map(({ icon, text }, i) => (
              <div
                key={i}
                className="flex items-start gap-4 rounded-xl border border-chart-0 bg-primary-foreground p-5 shadow-sm"
              >
                {icon}
                <p className="text-primary">{text}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      <Separator />

      {/* ── FEATURES ── */}
      <section className="bg-background py-20">
        <div className="mx-auto max-w-5xl px-6">
          <h2 className="mb-14 text-center text-3xl font-bold tracking-tight text-primary sm:text-4xl">
            Une seule interface pour toute votre recherche
          </h2>
          <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-4">
            {[
              {
                icon: <LayoutDashboard className="h-5 w-5 text-primary-foreground" />,
                title: "Suivi des candidatures",
                desc: "Visualisez toutes vos candidatures dans des tableaux clairs, par entreprise, par offre et par statut.",
              },
              {
                icon: <Bell className="h-5 w-5 text-primary-foreground" />,
                title: "Relances intelligentes",
                desc: "Définissez vos intervalles de relance et recevez des notifications au bon moment.",
              },
              {
                icon: <FolderOpen className="h-5 w-5 text-primary-foreground" />,
                title: "Documents associés",
                desc: "Stockez vos CV et lettres de motivation pour chaque offre et sachez exactement ce qui a été envoyé.",
              },
              {
                icon: <Lightbulb className="h-5 w-5 text-primary-foreground" />,
                title: "Conseils intégrés",
                desc: "Recevez des conseils contextuels pour améliorer votre organisation et vos chances de réponse.",
              },
            ].map(({ icon, title, desc }, i) => (
              <Card
                key={i}
                className="border border-chart-0 shadow-sm hover:shadow-md transition-shadow"
              >
                <CardHeader className="pb-2">
                  <div className="mb-2 flex h-9 w-9 items-center justify-center rounded-lg bg-primary">
                    {icon}
                  </div>
                  <CardTitle className="text-base text-primary">
                    {title}
                  </CardTitle>
                </CardHeader>
                <CardContent>
                  <p className="text-sm text-background-foreground">{desc}</p>
                </CardContent>
              </Card>
            ))}
          </div>
        </div>
      </section>

      <Separator />

      {/* ── BENEFITS ── */}
      <section className="bg-chart-0 py-20">
        <div className="mx-auto max-w-lg px-6 text-center">
          <h2 className="mb-10 text-3xl font-bold tracking-tight text-primary sm:text-4xl">
            Moins de stress. Plus de clarté.
          </h2>
          <div className="rounded-2xl border border-muted bg-primary-foreground p-8 shadow-sm">
            {[
              "Vision claire de votre avancement",
              "Relances faites au bon moment",
              "Candidatures cohérentes et traçables",
              "Moins d'oublis, plus de réponses",
            ].map((item, i) => (
              <div key={i} className="flex items-center gap-3 py-3">
                <div className="flex h-6 w-6 shrink-0 items-center justify-center rounded-full bg-primary">
                  <Check className="h-4 w-4 text-primary-foreground" />
                </div>
                <p className="font-medium text-primary">{item}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      <Separator />

      {/* ── HOW IT WORKS ── */}
      <section className="py-20">
        <div className="mx-auto max-w-4xl px-6">
          <h2 className="mb-14 text-center text-3xl font-bold tracking-tight text-primary sm:text-4xl">
            Simple à utiliser, dès aujourd'hui
          </h2>
          <div className="grid gap-6 sm:grid-cols-3">
            {[
              { step: "1", label: "S'inscrire" },
              { step: "2", label: "Ajoutez vos candidatures" },
              {
                step: "3",
                label: "Laissez l'application vous rappeler quand agir",
              },
            ].map(({ step, label }, i) => (
              <div
                key={i}
                className="flex flex-col items-center gap-3 rounded-xl border border-muted bg-chart-0 p-6 text-center shadow-sm"
              >
                <span className="flex h-12 w-12 items-center justify-center rounded-full bg-foreground text-xl font-extrabold text-background">
                  {step}
                </span>
                <p className="font-medium text-primary">{label}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* ── CTA ── */}
      <section className="bg-foreground py-24 text-center text-primary-foreground">
        <div className="mx-auto max-w-xl px-6">
          <h2 className="mb-6 text-3xl font-extrabold tracking-tight sm:text-4xl">
            Votre recherche d'emploi mérite mieux qu'un tableur
          </h2>
          <Button
            //asChild
            size="lg"
            className="bg-background hover:bg-chart-2 px-10 font-semibold text-foreground"
          >
            <Link href="/inscription">Commencer maintenant</Link>
          </Button>
          <p className="mt-4 text-sm text-muted">
            Gratuit — aucune carte bancaire requise
          </p>
        </div>
      </section>

      {/* ── FOOTER ── */}
      <footer className="bg-secondary-foreground py-8 text-center text-sm text-muted">
        © {new Date().getFullYear()} JobTracker. Tous droits réservés.
      </footer>
    </div>
  );
} 

