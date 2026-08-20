"use client"

import { useEffect, useState } from "react"
import { useForm } from "react-hook-form"
import { useGDP } from "@/lib/store"
import { Button } from "@/components/ui/button"
import {
    Card,
    CardContent,
    CardDescription,
    CardFooter,
    CardHeader,
    CardTitle,
} from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Alert, AlertDescription } from "@/components/ui/alert"
import { DeleteAccountDialog } from "@/components/delete-account-dialog"

interface ProfileFormState {
    pseudo: string
    email: string
    currentPassword: string
}

function ProfileForm() {
    const { user, updateProfile } = useGDP()
    const [success, setSuccess] = useState(false)
    const {
        register,
        handleSubmit,
        reset,
        formState: { errors, isSubmitting },
        setError,
    } = useForm<ProfileFormState>({
        defaultValues: { pseudo: "", email: "", currentPassword: "" },
    })

    useEffect(() => {
        if (user) {
            reset({ pseudo: user.pseudo, email: user.email, currentPassword: "" })
        }
    }, [user, reset])

    const onSubmit = async (data: ProfileFormState) => {
        setSuccess(false)
        try {
            await updateProfile(data)
            reset({ pseudo: data.pseudo, email: data.email, currentPassword: "" })
            setSuccess(true)
        } catch (err: unknown) {
            setError("root", {
                message: err instanceof Error ? err.message : "Erreur serveur",
            })
        }
    }

    return (
        <Card>
            <CardHeader>
                <CardTitle>Profil</CardTitle>
                <CardDescription>Modifiez votre pseudo et votre adresse email.</CardDescription>
            </CardHeader>
            <form onSubmit={handleSubmit(onSubmit)}>
                <CardContent className="flex flex-col gap-4">
                    <div className="flex flex-col gap-2">
                        <Label htmlFor="pseudo">Pseudo</Label>
                        <Input
                            id="pseudo"
                            aria-invalid={!!errors.pseudo}
                            {...register("pseudo", { required: "Le pseudo est requis" })}
                        />
                        {errors.pseudo && (
                            <span className="text-sm text-destructive">{errors.pseudo.message}</span>
                        )}
                    </div>

                    <div className="flex flex-col gap-2">
                        <Label htmlFor="email">Email</Label>
                        <Input
                            id="email"
                            type="email"
                            aria-invalid={!!errors.email}
                            {...register("email", {
                                required: "L'email est requis",
                                pattern: {
                                    value: /^[^\s@]+@[^\s@]+\.[^\s@]+$/,
                                    message: "Adresse email invalide",
                                },
                            })}
                        />
                        {errors.email && (
                            <span className="text-sm text-destructive">{errors.email.message}</span>
                        )}
                    </div>

                    <div className="flex flex-col gap-2">
                        <Label htmlFor="profile-current-password">Mot de passe actuel</Label>
                        <Input
                            id="profile-current-password"
                            type="password"
                            placeholder="••••••••"
                            aria-invalid={!!errors.currentPassword}
                            {...register("currentPassword", { required: "Le mot de passe actuel est requis" })}
                        />
                        {errors.currentPassword && (
                            <span className="text-sm text-destructive">{errors.currentPassword.message}</span>
                        )}
                    </div>

                    {errors.root && (
                        <Alert variant="destructive">
                            <AlertDescription>{errors.root.message}</AlertDescription>
                        </Alert>
                    )}
                    {success && (
                        <Alert>
                            <AlertDescription>Profil mis à jour.</AlertDescription>
                        </Alert>
                    )}
                </CardContent>
                <CardFooter>
                    <Button type="submit" disabled={isSubmitting}>
                        {isSubmitting ? "Enregistrement..." : "Enregistrer"}
                    </Button>
                </CardFooter>
            </form>
        </Card>
    )
}

interface PasswordFormState {
    currentPassword: string
    newPassword: string
    confirmPassword: string
}

function PasswordForm() {
    const { updatePassword } = useGDP()
    const [success, setSuccess] = useState(false)
    const {
        register,
        handleSubmit,
        reset,
        getValues,
        formState: { errors, isSubmitting },
        setError,
    } = useForm<PasswordFormState>({
        defaultValues: { currentPassword: "", newPassword: "", confirmPassword: "" },
    })

    const onSubmit = async (data: PasswordFormState) => {
        setSuccess(false)
        try {
            await updatePassword({
                currentPassword: data.currentPassword,
                newPassword: data.newPassword,
            })
            reset({ currentPassword: "", newPassword: "", confirmPassword: "" })
            setSuccess(true)
        } catch (err: unknown) {
            setError("root", {
                message: err instanceof Error ? err.message : "Erreur serveur",
            })
        }
    }

    return (
        <Card>
            <CardHeader>
                <CardTitle>Mot de passe</CardTitle>
                <CardDescription>Choisissez un nouveau mot de passe pour votre compte.</CardDescription>
            </CardHeader>
            <form onSubmit={handleSubmit(onSubmit)}>
                <CardContent className="flex flex-col gap-4">
                    <div className="flex flex-col gap-2">
                        <Label htmlFor="password-current-password">Mot de passe actuel</Label>
                        <Input
                            id="password-current-password"
                            type="password"
                            placeholder="••••••••"
                            aria-invalid={!!errors.currentPassword}
                            {...register("currentPassword", { required: "Le mot de passe actuel est requis" })}
                        />
                        {errors.currentPassword && (
                            <span className="text-sm text-destructive">{errors.currentPassword.message}</span>
                        )}
                    </div>

                    <div className="flex flex-col gap-2">
                        <Label htmlFor="new-password">Nouveau mot de passe</Label>
                        <Input
                            id="new-password"
                            type="password"
                            placeholder="••••••••"
                            aria-invalid={!!errors.newPassword}
                            {...register("newPassword", {
                                required: "Le nouveau mot de passe est requis",
                                minLength: {
                                    value: 8,
                                    message: "Le mot de passe doit contenir au moins 8 caractères",
                                },
                            })}
                        />
                        {errors.newPassword && (
                            <span className="text-sm text-destructive">{errors.newPassword.message}</span>
                        )}
                    </div>

                    <div className="flex flex-col gap-2">
                        <Label htmlFor="confirm-password">Confirmer le nouveau mot de passe</Label>
                        <Input
                            id="confirm-password"
                            type="password"
                            placeholder="••••••••"
                            aria-invalid={!!errors.confirmPassword}
                            {...register("confirmPassword", {
                                required: "La confirmation est requise",
                                validate: (value) =>
                                    value === getValues("newPassword") || "Les mots de passe ne correspondent pas",
                            })}
                        />
                        {errors.confirmPassword && (
                            <span className="text-sm text-destructive">{errors.confirmPassword.message}</span>
                        )}
                    </div>

                    {errors.root && (
                        <Alert variant="destructive">
                            <AlertDescription>{errors.root.message}</AlertDescription>
                        </Alert>
                    )}
                    {success && (
                        <Alert>
                            <AlertDescription>Mot de passe mis à jour.</AlertDescription>
                        </Alert>
                    )}
                </CardContent>
                <CardFooter>
                    <Button type="submit" disabled={isSubmitting}>
                        {isSubmitting ? "Enregistrement..." : "Changer le mot de passe"}
                    </Button>
                </CardFooter>
            </form>
        </Card>
    )
}

function DangerZone() {
    const [open, setOpen] = useState(false)

    return (
        <Card className="border-destructive/50">
            <CardHeader>
                <CardTitle className="text-destructive">Zone dangereuse</CardTitle>
                <CardDescription>
                    Supprimer votre compte est définitif : toutes vos entreprises, offres, candidatures
                    et contacts seront perdus.
                </CardDescription>
            </CardHeader>
            <CardFooter>
                <Button variant="destructive" onClick={() => setOpen(true)}>
                    Supprimer mon compte
                </Button>
            </CardFooter>
            <DeleteAccountDialog open={open} onOpenChange={setOpen} />
        </Card>
    )
}

export default function ComptePage() {
    return (
        <div className="flex flex-col gap-8 p-6 lg:p-8">
            <div>
                <h1 className="text-2xl font-bold tracking-tight">Mon compte</h1>
                <p className="text-sm text-muted-foreground mt-1">
                    Gérez vos informations personnelles et la sécurité de votre compte
                </p>
            </div>

            <div className="flex flex-col gap-6 max-w-xl">
                <ProfileForm />
                <PasswordForm />
                <DangerZone />
            </div>
        </div>
    )
}
