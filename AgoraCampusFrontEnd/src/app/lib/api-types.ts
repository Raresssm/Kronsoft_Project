export type AppUserSummary = {
  id: number;
  keycloakId: string;
  email: string;
  username: string;
  createdAt: string;
  accountType: "INDIVIDUAL" | "ORGANIZATION" | null;
  profileId: number | null;
  individualProfileId: number | null;
  organizationProfileId: number | null;
  displayName: string | null;
};

export type PostResponse = {
  postId: number;
  authorUserId: number;
  content: string;
  mediaUrl: string | null;
  createdAt: string;
};

export type CommentResponse = {
  commentId: number;
  postId: number;
  authorUserId: number;
  content: string;
  createdAt: string;
};

export type ReactionResponse = {
  reactionId: number;
  postId: number;
  authorUserId: number;
  reactionType: "LIKE" | "LOVE" | "CELEBRATE" | "SUPPORT";
  createdAt: string;
};

export type OpportunityType = "VOLUNTEERING" | "INTERNSHIP" | "STUDENT_PROJECT" | "COMPETITION";

export type OpportunityResponse = {
  opportunityId: number;
  postedByUserId: number;
  type: OpportunityType;
  title: string;
  location: string;
  period: string;
  description: string;
  additionalInfo: string | null;
  createdAt: string;
  postingProfile: {
    profileType: "INDIVIDUAL" | "ORGANIZATION";
    organizationProfileId: number | null;
    individualProfileId: number | null;
    profileId: number;
    appUserId: number;
    displayName: string;
  } | null;
};

export type OpportunityApplicationResponse = {
  applicationId: number;
  opportunityId: number;
  applicantUserId: number;
  applicantUsername: string;
  status: "PENDING" | "ACCEPTED" | "REJECTED";
  appliedAt: string;
};

export type ConnectionResponse = {
  connectionId: number;
  requesterUserId: number;
  receiverUserId: number;
  status: "PENDING" | "ACCEPTED" | "REJECTED";
  createdAt: string;
};

export type MessageResponse = {
  messageId: number;
  senderUserId: number;
  receiverUserId: number;
  content: string;
  sentAt: string;
  acknowledged: boolean;
};

export type BackgroundResponse = {
  backgroundId: number;
  individualProfileId: number;
  firstName: string | null;
  lastName: string | null;
  headline: string | null;
  profilePicture: string | null;
  type: "EDUCATION" | "WORK_EXPERIENCE" | "PROJECT";
  title: string;
  description: string | null;
  startDate: string;
  endDate: string | null;
  currentlyOngoing: boolean;
};

export type IndividualProfileResponse = {
  id: number;
  profileId: number;
  headline: string | null;
  description: string | null;
  location: string | null;
  website: string | null;
  profilePicture: string | null;
  coverImage: string | null;
  updatedAt: string | null;
  firstName: string | null;
  lastName: string | null;
  phone: string | null;
  cvDocument: string | null;
  backgrounds: BackgroundResponse[];
};

export type OrganizationProfileResponse = {
  id: number;
  profileId: number;
  headline: string | null;
  description: string | null;
  location: string | null;
  website: string | null;
  profilePicture: string | null;
  coverImage: string | null;
  updatedAt: string | null;
  organizationName: string | null;
  phone: string | null;
  industry: string | null;
  specialties: string | null;
};

export function formatRelativeTime(value: string) {
  const then = new Date(value).getTime();
  if (Number.isNaN(then)) {
    return "";
  }

  const diffMs = Date.now() - then;
  const diffMinutes = Math.max(0, Math.floor(diffMs / 60000));
  if (diffMinutes < 1) return "Just now";
  if (diffMinutes < 60) return `${diffMinutes} min ago`;

  const diffHours = Math.floor(diffMinutes / 60);
  if (diffHours < 24) return `${diffHours} h ago`;

  const diffDays = Math.floor(diffHours / 24);
  return diffDays === 1 ? "Yesterday" : `${diffDays} days ago`;
}
