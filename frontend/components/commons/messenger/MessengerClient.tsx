"use client";

import { useState, useRef, useEffect } from "react";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { ScrollArea } from "@/components/ui/scroll-area";
import { Card } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import {
  Send,
  Search,
  MoreVertical,
  ArrowDown,
  Smile,
  Paperclip,
  Phone,
  Video,
  Info,
} from "lucide-react";
import { cn, formatDateAgo } from "@/lib/utils";
import { mockConversations, mockMessages } from "@/services/mockData";

export default function MessengerClient() {
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
  const messagesEndRef = useRef<HTMLDivElement>(null);
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
    messagesEndRef.current?.scrollIntoView({
      behavior: smooth ? "smooth" : "auto",
      block: "end",
    });
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

        {/* Conversations List */}
        <ScrollArea className="flex-1">
          {filteredConversations.length === 0 ? (
            <div className="flex items-center justify-center h-full text-muted-foreground">
              Không tìm thấy cuộc hội thoại
            </div>
          ) : (
            filteredConversations.map((conversation) => (
              <div
                key={conversation.id}
                onClick={() => setSelectedConversation(conversation)}
                className={cn(
                  "flex items-center gap-3 p-4 cursor-pointer transition-colors hover:bg-accent",
                  selectedConversation?.id === conversation.id && "bg-accent",
                )}
              >
                <div className="relative">
                  <Avatar>
                    <AvatarImage src={conversation.participant.avatarUrl} />
                    <AvatarFallback className="bg-primary text-primary-foreground">
                      {conversation.participant.username
                        .charAt(0)
                        .toUpperCase()}
                    </AvatarFallback>
                  </Avatar>
                  {conversation.participant.isOnline && (
                    <span className="absolute bottom-0 right-0 w-3 h-3 bg-secondary border-2 border-background rounded-full" />
                  )}
                </div>

                <div className="flex-1 min-w-0">
                  <div className="flex items-center justify-between mb-1">
                    <h3 className="font-semibold text-sm truncate">
                      {conversation.participant.username}
                    </h3>
                    {conversation.lastMessage && (
                      <span className="text-xs text-muted-foreground">
                        {formatDateAgo(`${conversation.lastMessage.createdAt}`)}
                      </span>
                    )}
                  </div>
                  <div className="flex items-center justify-between">
                    <p className="text-sm text-muted-foreground truncate">
                      {conversation.lastMessage?.senderId === currentUserId &&
                        "Bạn: "}
                      {conversation.lastMessage?.content || "Chưa có tin nhắn"}
                    </p>
                    {conversation.unreadCount > 0 && (
                      <Badge className="bg-primary text-primary-foreground ml-2">
                        {conversation.unreadCount}
                      </Badge>
                    )}
                  </div>
                </div>
              </div>
            ))
          )}
        </ScrollArea>
      </div>

      {/* Right Side - Chat Area */}
      {!selectedConversation ? (
        <div className="flex-1 flex items-center justify-center">
          <div className="text-center">
            <div className="mb-4 inline-flex items-center justify-center w-20 h-20 rounded-full bg-primary/10">
              <Send className="w-10 h-10 text-primary" />
            </div>
            <h2 className="text-2xl font-semibold mb-2">
              Xin chọn cuộc hội thoại
            </h2>
            <p className="text-muted-foreground">
              Chọn một cuộc hội thoại từ danh sách bên trái để bắt đầu nhắn tin
            </p>
          </div>
        </div>
      ) : (
        <div className="flex-1 flex flex-col">
          {/* Chat Header */}
          <div className="h-16 border-b border-border flex items-center justify-between px-6">
            <div className="flex items-center gap-3">
              <div className="relative">
                <Avatar>
                  <AvatarImage
                    src={selectedConversation.participant.avatarUrl}
                  />
                  <AvatarFallback className="bg-primary text-primary-foreground">
                    {selectedConversation.participant.username
                      .charAt(0)
                      .toUpperCase()}
                  </AvatarFallback>
                </Avatar>
                {selectedConversation.participant.isOnline && (
                  <span className="absolute bottom-0 right-0 w-3 h-3 bg-secondary border-2 border-background rounded-full" />
                )}
              </div>
              <div>
                <h2 className="font-semibold">
                  {selectedConversation.participant.username}
                </h2>
                <p className="text-xs text-muted-foreground">
                  {selectedConversation.participant.isOnline
                    ? "Đang hoạt động"
                    : "Không hoạt động"}
                </p>
              </div>
            </div>

            <div className="flex items-center gap-2">
              <Button variant="ghost" size="icon">
                <Phone className="w-5 h-5" />
              </Button>
              <Button variant="ghost" size="icon">
                <Video className="w-5 h-5" />
              </Button>
              <Button variant="ghost" size="icon">
                <Info className="w-5 h-5" />
              </Button>
              <Button variant="ghost" size="icon">
                <MoreVertical className="w-5 h-5" />
              </Button>
            </div>
          </div>

          {/* Messages Area */}
          <div className="flex-1 relative">
            <ScrollArea className="h-full p-6" onScrollCapture={handleScroll}>
              <div ref={scrollAreaRef}>
                {messages.map((message, index) => {
                  const isOwn = message.senderId === currentUserId;
                  const showTimestamp =
                    index === 0 ||
                    new Date(message.createdAt).getTime() -
                      new Date(messages[index - 1].createdAt).getTime() >
                      300000; // 5 minutes

                  return (
                    <div key={message.id} className="mb-4">
                      {showTimestamp && (
                        <div className="flex justify-center mb-4">
                          <span className="text-xs text-muted-foreground bg-muted px-3 py-1 rounded-full">
                            {formatMessageTime(new Date(message.createdAt))}
                          </span>
                        </div>
                      )}
                      <div
                        className={cn(
                          "flex",
                          isOwn ? "justify-end" : "justify-start",
                        )}
                      >
                        {!isOwn && (
                          <Avatar className="w-8 h-8 mr-2">
                            <AvatarImage
                              src={selectedConversation.participant.avatarUrl}
                            />
                            <AvatarFallback className="bg-primary text-primary-foreground text-xs">
                              {selectedConversation.participant.username
                                .charAt(0)
                                .toUpperCase()}
                            </AvatarFallback>
                          </Avatar>
                        )}
                        <div
                          className={cn(
                            "max-w-[70%] rounded-2xl px-4 py-2",
                            isOwn
                              ? "bg-primary text-primary-foreground"
                              : "bg-secondary text-secondary-foreground",
                          )}
                        >
                          <p className="text-sm wrap-break-word">
                            {message.content}
                          </p>
                        </div>
                      </div>
                    </div>
                  );
                })}
                <div ref={messagesEndRef} />
              </div>
            </ScrollArea>

            {/* Scroll to Bottom Button / New Message Preview */}
            {showScrollButton && (
              <div className="absolute bottom-4 left-1/2 transform -translate-x-1/2">
                {newMessagePreview ? (
                  <Card
                    className="px-4 py-2 cursor-pointer shadow-lg hover:shadow-xl transition-shadow bg-card border-2 border-primary"
                    onClick={() => scrollToBottom(true)}
                  >
                    <div className="flex items-center gap-2">
                      <div className="flex-1">
                        <p className="text-sm font-medium mb-1">Tin nhắn mới</p>
                        <p className="text-xs text-muted-foreground">
                          {newMessagePreview}
                        </p>
                      </div>
                      <ArrowDown className="w-5 h-5 text-primary" />
                    </div>
                  </Card>
                ) : (
                  <Button
                    size="icon"
                    className="rounded-full shadow-lg hover:shadow-xl transition-shadow"
                    onClick={() => scrollToBottom(true)}
                  >
                    <ArrowDown className="w-5 h-5" />
                  </Button>
                )}
              </div>
            )}
          </div>

          {/* Message Input */}
          <div className="border-t border-border p-4">
            <div className="flex items-center gap-2">
              <Button variant="ghost" size="icon" className="shrink-0">
                <Paperclip className="w-5 h-5" />
              </Button>
              <Button variant="ghost" size="icon" className="shrink-0">
                <Smile className="w-5 h-5" />
              </Button>
              <Input
                placeholder="Nhập tin nhắn..."
                value={messageInput}
                onChange={(e) => setMessageInput(e.target.value)}
                onKeyPress={(e) => e.key === "Enter" && handleSendMessage()}
                className="flex-1"
              />
              <Button onClick={handleSendMessage} className="shrink-0">
                <Send className="w-5 h-5" />
              </Button>
            </div>

            {/* Demo Button to Simulate New Message */}
            <div className="mt-2 flex justify-center">
              <Button
                variant="outline"
                size="sm"
                onClick={simulateNewMessage}
                className="text-xs"
              >
                Demo: Nhận tin nhắn mới
              </Button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
