"use client";

import { useState, useRef, useEffect } from "react";
import { Input } from "@/components/ui/input";
import { Search } from "lucide-react";
import { mockConversations, mockMessages } from "@/services/mockData";
import { Conversations } from "./Conversations";
import { EmptyChatState } from "./EmptyChatState";
import { ChatHeader } from "./ChatHeader";
import { MessagesArea } from "./MessagesArea";
import { MessageInput } from "./MessageInput";


const MessengerClient = () => {
  // States
  const [conversations, setConversations] = useState<IConversation[]>([]);
  const [selectedConversation, setSelectedConversation] =
    useState<IConversation | null>(null);
  const [messages, setMessages] = useState<IMessage[]>([]);
  const [messageInput, setMessageInput] = useState("");
  const [searchQuery, setSearchQuery] = useState("");
  const [showScrollButton, setShowScrollButton] = useState(false);
  const [newMessagePreview, setNewMessagePreview] = useState<string | null>(
    null,
  );
  const [isUserScrolling, setIsUserScrolling] = useState(false);

  // Refs
  const scrollAreaRef = useRef<HTMLDivElement>(null);
  const currentUserId = "current-user-id"; // Replace with actual user ID

  // Mock data - Replace with actual API calls
  useEffect(() => {
    setConversations(mockConversations);
  }, []);

  // Load messages when conversation is selected
  useEffect(() => {
    if (selectedConversation) {
      setMessages(mockMessages);
      scrollToBottom(true);
    }
  }, [selectedConversation]);

  // Auto scroll to bottom when new message arrives
  useEffect(() => {
    if (!isUserScrolling) {
      scrollToBottom(true);
    }
  }, [messages]);

  // Scroll to bottom function
  const scrollToBottom = (smooth: boolean = false) => {
    const messagesEndElement = document.querySelector("[data-messages-end]");
    if (messagesEndElement) {
      messagesEndElement.scrollIntoView({
        behavior: smooth ? "smooth" : "auto",
        block: "end",
      });
    }
    setShowScrollButton(false);
    setNewMessagePreview(null);
    setIsUserScrolling(false);
  };

  // Handle scroll event
  const handleScroll = (event: React.UIEvent<HTMLDivElement>) => {
    const target = event.target as HTMLDivElement;
    const scrollTop = target.scrollTop;
    const scrollHeight = target.scrollHeight;
    const clientHeight = target.clientHeight;

    // Check if user scrolled up more than 200px from bottom
    const distanceFromBottom = scrollHeight - scrollTop - clientHeight;

    if (distanceFromBottom > 200) {
      setShowScrollButton(true);
      setIsUserScrolling(true);
    } else {
      setShowScrollButton(false);
      setNewMessagePreview(null);
      setIsUserScrolling(false);
    }
  };

  // Send message
  const handleSendMessage = () => {
    if (!messageInput.trim() || !selectedConversation) return;

    const newMessage: IMessage = {
      id: Date.now().toString(),
      content: messageInput,
      senderId: currentUserId,
      createdAt: `${new Date()}`,
      isRead: false,
    };

    setMessages((prev) => [...prev, newMessage]);
    setMessageInput("");
  };

  // Simulate receiving a new message (for demo purposes)
  const simulateNewMessage = () => {
    if (!selectedConversation) return;

    const newMessage: IMessage = {
      id: Date.now().toString(),
      content: "Đây là tin nhắn mới từ đối phương!",
      senderId: selectedConversation.participant.id,
      createdAt: `${new Date()}`,
      isRead: false,
    };

    setMessages((prev) => [...prev, newMessage]);

    // If user is scrolling up, show preview instead of auto-scrolling
    if (isUserScrolling) {
      const preview =
        newMessage.content.length > 30
          ? newMessage.content.substring(0, 30) + "..."
          : newMessage.content;
      setNewMessagePreview(preview);
    }
  };

  // Filter conversations based on search
  const filteredConversations = conversations.filter((conv) =>
    conv.participant.username.toLowerCase().includes(searchQuery.toLowerCase()),
  );

  // Format message time
  const formatMessageTime = (date: Date) => {
    return date.toLocaleTimeString("vi-VN", {
      hour: "2-digit",
      minute: "2-digit",
    });
  };

  return (
    <div className="flex h-[calc(100vh-4rem)] bg-background">
      {/* Left Sidebar - Conversations List */}
      <div className="w-80 border-r border-border flex flex-col">
        {/* Search Header */}
        <div className="p-4 border-b border-border">
          <div className="relative">
            <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-muted-foreground w-4 h-4" />
            <Input
              placeholder="Tìm kiếm cuộc hội thoại..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="pl-10"
            />
          </div>
        </div>

        <Conversations
          conversations={filteredConversations}
          selectedConversation={selectedConversation}
          onSelectConversation={setSelectedConversation}
          currentUserId={currentUserId}
        />
      </div>

      {/* Right Side - Chat Area */}
      {!selectedConversation ? (
        <EmptyChatState />
      ) : (
        <div className="flex-1 flex flex-col">
          <ChatHeader selectedConversation={selectedConversation} />

          <MessagesArea
            messages={messages}
            selectedConversation={selectedConversation}
            currentUserId={currentUserId}
            showScrollButton={showScrollButton}
            newMessagePreview={newMessagePreview}
            onScroll={handleScroll}
            onScrollToBottom={() => scrollToBottom(true)}
            formatMessageTime={formatMessageTime}
          />

          <MessageInput
            messageInput={messageInput}
            onMessageChange={setMessageInput}
            onSendMessage={handleSendMessage}
            onSimulateNewMessage={simulateNewMessage}
          />
        </div>
      )}
    </div>
  );
};

export default MessengerClient;
