"use client"

import { useState } from "react"
import { useGDP } from "@/lib/store"
import { ContactRow } from "@/components/businesses/contact-row"
import { Card, CardContent } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import {
    Select,
    SelectContent,
    SelectItem,
    SelectTrigger,
    SelectValue,
} from "@/components/ui/select"
import { Users, Search } from "lucide-react"

const ALL = "Tout les contacts"

export default function ContactsPage() {
    const { businesses } = useGDP()
    const [search, setSearch] = useState("")
    const [selectedBiz, setSelectedBiz] = useState(ALL)

    const contacts = businesses.flatMap((biz) =>
        biz.professionalsList.map((p) => ({ ...p, businessName: biz.name, businessId: biz.id }))
    )

    const filtered = contacts.filter((p) => {
        const q = search.toLowerCase()
        const matchesSearch =
            p.firstName.toLowerCase().includes(q) ||
            p.lastName.toLowerCase().includes(q)
        const matchesBiz =
            selectedBiz === ALL || p.businessId === Number(selectedBiz)

        return matchesSearch && matchesBiz
    })

    return (
        <div className="p-6 space-y-6">
            <div>
                <h1 className="text-2xl font-bold">Contacts</h1>
                <p className="text-muted-foreground flex items-center gap-1 mt-1">
                    <Users className="w-4 h-4" />
                    {contacts.length} contact{contacts.length > 1 ? "s" : ""} au total
                </p>
            </div>

            <div className="flex flex-col sm:flex-row gap-3">
                <div className="relative flex-1">
                    <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
                    <Input
                        placeholder="Rechercher par prénom ou nom..."
                        value={search}
                        onChange={(e) => setSearch(e.target.value)}
                        className="pl-9"
                    />
                </div>
                <Select value={selectedBiz} onValueChange={(value) => setSelectedBiz(value ?? ALL)}>
                    <SelectTrigger className="w-full sm:w-[220px]">
                        <SelectValue placeholder="Toutes les entreprises" />
                    </SelectTrigger>
                    <SelectContent>
                        <SelectItem value={ALL}>Toutes les entreprises</SelectItem>
                        {businesses.map((biz) => (
                            <SelectItem key={biz.id} value={String(biz.id)}>
                                {biz.name}
                            </SelectItem>
                        ))}
                    </SelectContent>
                </Select>
            </div>

            {contacts.length === 0 ? (
                <Card>
                    <CardContent className="py-12 text-center text-muted-foreground">
                        Aucun contact trouvé
                    </CardContent>
                </Card>
            ) : filtered.length === 0 ? (
                <Card>
                    <CardContent className="py-12 text-center text-muted-foreground">
                        Aucun contact correspondant à la recherche
                    </CardContent>
                </Card>
            ) : (
                <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
                    {filtered.map((p) => (
                        <div key={p.id} className="space-y-1">
                            <p className="text-xs text-muted-foreground px-1">{p.businessName}</p>
                            <ContactRow
                                pro={p}
                                onDelete={(id) => console.log("delete", id)}
                            />
                        </div>
                    ))}
                </div>
            )}
        </div>
    )
}