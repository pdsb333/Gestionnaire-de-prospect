export interface Auth {
  email: string
  pseudo: string
  password: string
}

export interface Business {
  id: number;
  name: string;
  description: string;
  recruitmentServiceContact: string;
  jobOffersList: JobOffer[];
  professionalsList: Professional[];
}

export interface Professional {
  id: number;
  lastName: string;
  firstName: string;
  job: string;
  contact: string;
}

export interface JobOffer {
  id: number;
  name: string;
  link: string;
  relaunchFrequency: number;
  application: Application | null;
}

export interface Application {
  id: number;
  initialApplicationDate: string;
  dateRelaunch: string;
  historyOfRelaunches: string[];
}